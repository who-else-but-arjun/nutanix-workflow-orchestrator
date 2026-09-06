package com.flowforge.temporal;

import com.flowforge.domain.execution.ExecutionState;
import com.flowforge.domain.execution.NodeExecutionState;
import com.flowforge.domain.task.TaskRequest;
import com.flowforge.domain.task.TaskResult;
import com.flowforge.domain.workflow.WorkflowDefinition;
import com.flowforge.domain.workflow.WorkflowNode;
import com.flowforge.engine.registry.ExecutorRegistry;
import com.flowforge.persistence.ExecutionNodeRecord;
import com.flowforge.persistence.ExecutionRecord;
import com.flowforge.persistence.InMemoryWorkflowStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class FlowForgeActivitiesImpl implements FlowForgeActivities {
    private final ExecutorRegistry executorRegistry;
    private final InMemoryWorkflowStore store;
    private final String namespace;
    private final String taskQueue;

    public FlowForgeActivitiesImpl(
            ExecutorRegistry executorRegistry,
            InMemoryWorkflowStore store,
            @Value("${flowforge.temporal.namespace}") String namespace,
            @Value("${flowforge.temporal.task-queue}") String taskQueue) {
        this.executorRegistry = executorRegistry;
        this.store = store;
        this.namespace = namespace;
        this.taskQueue = taskQueue;
    }

    @Override
    public TaskResult execute(TaskRequest request) {
        return executorRegistry.resolve(request.type()).execute(request);
    }

    @Override
    public void publish(ExecutionUpdate update) {
        ExecutionRecord existing = store.findExecution(update.executionId()).orElse(null);
        Instant now = Instant.now();
        Map<String, ExecutionNodeRecord> existingNodes = new LinkedHashMap<>();
        if (existing != null) {
            existing.nodes().forEach(node -> existingNodes.put(node.nodeId(), node));
        }

        List<ExecutionNodeRecord> nodes = new ArrayList<>();
        for (WorkflowNode definitionNode : update.definition().nodes()) {
            NodeExecutionState nodeState = update.nodeStates().getOrDefault(definitionNode.id(),
                    NodeExecutionState.PENDING);
            ExecutionNodeRecord previous = existingNodes.get(definitionNode.id());
            TaskResult result = update.results().get(definitionNode.id());
            Instant startedAt = previous == null ? null : previous.startedAt();
            if (startedAt == null && nodeState != NodeExecutionState.PENDING
                    && nodeState != NodeExecutionState.SKIPPED) {
                startedAt = now;
            }
            Instant finishedAt = previous == null ? null : previous.finishedAt();
            if ((nodeState == NodeExecutionState.SUCCEEDED || nodeState == NodeExecutionState.FAILED)
                    && finishedAt == null) {
                finishedAt = now;
            }
            nodes.add(new ExecutionNodeRecord(
                    definitionNode.id(),
                    definitionNode.type(),
                    nodeState,
                    startedAt,
                    finishedAt,
                    result == null ? Map.of() : result.output(),
                    result == null ? null : result.error()));
        }

        Instant startedAt = existing == null ? now : existing.startedAt();
        Instant finishedAt = isTerminal(update.state()) ? now : null;
        store.saveExecution(new ExecutionRecord(
                update.executionId(),
                update.definition().workflowName(),
                update.definition().version(),
                update.state(),
                startedAt,
                finishedAt,
                update.input(),
                List.copyOf(nodes),
                update.failureReason(),
                update.compensationExecutionId(),
                existing == null ? update.executionId() : existing.temporalWorkflowId(),
                existing == null ? null : existing.temporalRunId(),
                existing == null ? namespace : existing.temporalNamespace(),
                existing == null ? taskQueue : existing.temporalTaskQueue()));
    }

    private boolean isTerminal(ExecutionState state) {
        return state == ExecutionState.SUCCEEDED
                || state == ExecutionState.FAILED
                || state == ExecutionState.COMPENSATED
                || state == ExecutionState.COMPENSATION_FAILED;
    }

    @Override
    public WorkflowDefinition loadWorkflow(String name) {
        return store.findWorkflow(name).orElse(null);
    }
}
