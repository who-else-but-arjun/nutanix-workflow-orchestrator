package com.flowforge.temporal;

import com.flowforge.domain.execution.ExecutionState;
import com.flowforge.domain.execution.NodeExecutionState;
import com.flowforge.domain.task.TaskRequest;
import com.flowforge.domain.task.TaskResult;
import com.flowforge.domain.workflow.WorkflowDefinition;
import com.flowforge.domain.workflow.WorkflowNode;
import com.flowforge.engine.scheduler.ReadyNodeResolver;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FlowForgeWorkflowImpl implements FlowForgeWorkflow {
    private final ReadyNodeResolver readyNodeResolver = new ReadyNodeResolver();
    private final FlowForgeActivities activities = Workflow.newActivityStub(
            FlowForgeActivities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofMinutes(5)).build());

    @Override
    public ExecutionState run(TemporalWorkflowRequest request) {
        WorkflowDefinition definition = request.definition();
        Map<String, NodeExecutionState> states = initialStates(definition);
        Map<String, TaskResult> results = new LinkedHashMap<>();
        Map<String, Object> outputs = new LinkedHashMap<>();
        publish(request, ExecutionState.RUNNING, states, results, null, null);

        while (states.containsValue(NodeExecutionState.PENDING)) {
            List<WorkflowNode> skipped = readyNodeResolver.resolveSkipped(definition, states, request.input(), outputs);
            if (!skipped.isEmpty()) {
                skipped.forEach(node -> states.put(node.id(), NodeExecutionState.SKIPPED));
                publish(request, ExecutionState.RUNNING, states, results, null, null);
            }

            List<WorkflowNode> ready = readyNodeResolver.resolve(definition, states, request.input(), outputs);
            if (ready.isEmpty()) {
                if (!states.containsValue(NodeExecutionState.PENDING)) {
                    break;
                }
                publish(request, ExecutionState.FAILED, states, results, "No runnable nodes remain", null);
                return ExecutionState.FAILED;
            }

            Map<WorkflowNode, Promise<TaskResult>> tasks = new LinkedHashMap<>();
            for (WorkflowNode node : ready) {
                states.put(node.id(), NodeExecutionState.RUNNING);
                tasks.put(node, Async.function(activities::execute, new TaskRequest(
                        node.id(), definition.flowId(), node.type(), node.config(), request.input(),
                        Map.copyOf(outputs))));
            }
            publish(request, ExecutionState.RUNNING, states, results, null, null);

            String failure = null;
            for (WorkflowNode node : ready) {
                TaskResult result = tasks.get(node).get();
                results.put(node.id(), result);
                if (result.success()) {
                    states.put(node.id(), NodeExecutionState.SUCCEEDED);
                    outputs.put(node.id(), result.output());
                } else {
                    states.put(node.id(), NodeExecutionState.FAILED);
                    failure = failure == null ? result.error() : failure;
                }
            }

            if (failure != null) {
                states.replaceAll((nodeId, nodeState) -> nodeState == NodeExecutionState.PENDING
                        ? NodeExecutionState.SKIPPED
                        : nodeState);
                return handleFailure(request, states, results, failure);
            }
            publish(request, ExecutionState.RUNNING, states, results, null, null);
        }

        publish(request, ExecutionState.SUCCEEDED, states, results, null, null);
        return ExecutionState.SUCCEEDED;
    }

    private ExecutionState handleFailure(
            TemporalWorkflowRequest request,
            Map<String, NodeExecutionState> states,
            Map<String, TaskResult> results,
            String failureReason) {
        WorkflowDefinition definition = request.definition();
        if (definition.onFailure() == null || !definition.onFailure().hasCompensation()) {
            publish(request, ExecutionState.FAILED, states, results, failureReason, null);
            return ExecutionState.FAILED;
        }

        String compensationExecutionId = request.executionId() + "-compensation";
        publish(request, ExecutionState.COMPENSATING, states, results, failureReason, compensationExecutionId);

        WorkflowDefinition compensation = activities.loadWorkflow(definition.onFailure().compensationFlow());
        if (compensation == null) {
            publish(request, ExecutionState.COMPENSATION_FAILED, states, results, failureReason,
                    compensationExecutionId);
            return ExecutionState.COMPENSATION_FAILED;
        }

        FlowForgeWorkflow compensationWorkflow = Workflow.newChildWorkflowStub(
                FlowForgeWorkflow.class,
                ChildWorkflowOptions.newBuilder().setWorkflowId(compensationExecutionId).build());
        ExecutionState compensationState = compensationWorkflow.run(new TemporalWorkflowRequest(
                compensationExecutionId,
                compensation,
                request.input()));
        ExecutionState finalState = compensationState == ExecutionState.SUCCEEDED ? ExecutionState.COMPENSATED
                : ExecutionState.COMPENSATION_FAILED;
        publish(request, finalState, states, results, failureReason, compensationExecutionId);
        return finalState;
    }

    private Map<String, NodeExecutionState> initialStates(WorkflowDefinition definition) {
        Map<String, NodeExecutionState> states = new LinkedHashMap<>();
        definition.nodes().forEach(node -> states.put(node.id(), NodeExecutionState.PENDING));
        return states;
    }

    private void publish(
            TemporalWorkflowRequest request,
            ExecutionState state,
            Map<String, NodeExecutionState> states,
            Map<String, TaskResult> results,
            String failureReason,
            String compensationExecutionId) {
        activities.publish(new ExecutionUpdate(
                request.executionId(), request.definition(), request.input(), state,
                Map.copyOf(states), Map.copyOf(results), failureReason, compensationExecutionId));
    }
}
