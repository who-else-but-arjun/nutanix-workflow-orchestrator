package com.flowforge.engine.scheduler;

import com.flowforge.domain.execution.NodeExecutionState;
import com.flowforge.domain.workflow.WorkflowDefinition;
import com.flowforge.domain.workflow.WorkflowEdge;
import com.flowforge.domain.workflow.WorkflowNode;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class ReadyNodeResolver {
    public List<WorkflowNode> resolve(WorkflowDefinition definition, Map<String, NodeExecutionState> states) {
        return resolve(definition, states, Map.of(), Map.of());
    }

    public List<WorkflowNode> resolve(
            WorkflowDefinition definition,
            Map<String, NodeExecutionState> states,
            Map<String, Object> workflowInput,
            Map<String, Object> outputs
    ) {
        return definition.nodes().stream()
                .filter(node -> states.get(node.id()) == NodeExecutionState.PENDING)
                .filter(node -> dependenciesSucceeded(definition, states, workflowInput, outputs, node.id()))
                .sorted(Comparator.comparing(WorkflowNode::id))
                .toList();
    }

    public List<WorkflowNode> resolveSkipped(
            WorkflowDefinition definition,
            Map<String, NodeExecutionState> states,
            Map<String, Object> workflowInput,
            Map<String, Object> outputs
    ) {
        return definition.nodes().stream()
                .filter(node -> states.get(node.id()) == NodeExecutionState.PENDING)
                .filter(node -> branchCannotRun(definition, states, workflowInput, outputs, node.id()))
                .sorted(Comparator.comparing(WorkflowNode::id))
                .toList();
    }

    private boolean dependenciesSucceeded(
            WorkflowDefinition definition,
            Map<String, NodeExecutionState> states,
            Map<String, Object> workflowInput,
            Map<String, Object> outputs,
            String nodeId
    ) {
        List<WorkflowEdge> incoming = incoming(definition, nodeId);
        return incoming.stream()
                .allMatch(edge -> edgeSatisfied(edge, states, workflowInput, outputs));
    }

    private boolean branchCannotRun(
            WorkflowDefinition definition,
            Map<String, NodeExecutionState> states,
            Map<String, Object> workflowInput,
            Map<String, Object> outputs,
            String nodeId
    ) {
        List<WorkflowEdge> incoming = incoming(definition, nodeId);
        return !incoming.isEmpty()
                && incoming.stream().allMatch(edge -> dependencyTerminal(states.get(edge.from())))
                && incoming.stream().anyMatch(edge -> !edgeSatisfied(edge, states, workflowInput, outputs));
    }

    private List<WorkflowEdge> incoming(WorkflowDefinition definition, String nodeId) {
        return definition.edges().stream()
                .filter(edge -> edge.to().equals(nodeId))
                .toList();
    }

    private boolean edgeSatisfied(
            WorkflowEdge edge,
            Map<String, NodeExecutionState> states,
            Map<String, Object> workflowInput,
            Map<String, Object> outputs
    ) {
        return states.get(edge.from()) == NodeExecutionState.SUCCEEDED
                && ConditionEvaluator.matches(edge.condition(), workflowInput, outputs);
    }

    private boolean dependencyTerminal(NodeExecutionState state) {
        return state == NodeExecutionState.SUCCEEDED
                || state == NodeExecutionState.FAILED
                || state == NodeExecutionState.SKIPPED;
    }
}
