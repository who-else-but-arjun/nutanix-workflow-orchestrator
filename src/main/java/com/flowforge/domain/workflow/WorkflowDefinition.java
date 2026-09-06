package com.flowforge.domain.workflow;

import java.util.List;
import java.util.Map;

public record WorkflowDefinition(
        String workflowName,
        String flowId,
        String version,
        Map<String, Object> input,
        List<WorkflowNode> nodes,
        List<WorkflowEdge> edges,
        FailurePolicy onFailure) {
    public WorkflowDefinition(
            String workflowName,
            String version,
            Map<String, Object> input,
            List<WorkflowNode> nodes,
            List<WorkflowEdge> edges,
            FailurePolicy onFailure) {
        this(workflowName, workflowName, version, input, nodes, edges, onFailure);
    }

    public WorkflowDefinition {
        input = input == null ? Map.of() : Map.copyOf(input);
        nodes = nodes == null ? List.of() : List.copyOf(nodes);
        edges = edges == null ? List.of() : List.copyOf(edges);
    }
}
