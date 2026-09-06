package com.flowforge.api.dto;

import com.flowforge.domain.workflow.FailurePolicy;
import com.flowforge.domain.workflow.WorkflowDefinition;
import com.flowforge.domain.workflow.WorkflowEdge;
import com.flowforge.domain.workflow.WorkflowNode;

import java.util.List;

public final class WorkflowMapper {
        private WorkflowMapper() {
        }

        public static WorkflowDefinition toDomain(WorkflowDefinitionRequest request) {
                List<WorkflowNode> nodes = request.nodes().stream()
                                .map(node -> new WorkflowNode(node.id(), node.type(), node.config()))
                                .toList();
                List<WorkflowEdge> edges = request.edges() == null ? List.of()
                                : request.edges().stream()
                                                .map(edge -> new WorkflowEdge(edge.get(0), edge.get(1),
                                                                edge.size() > 2 ? edge.get(2) : null))
                                                .toList();
                FailurePolicy failurePolicy = request.onFailure() == null
                                ? null
                                : new FailurePolicy(request.onFailure().compensationFlow());
                return new WorkflowDefinition(
                                request.workflowName(),
                                request.flowId() == null || request.flowId().isBlank() ? request.workflowName()
                                                : request.flowId(),
                                request.version() == null ? "1.0" : request.version(),
                                request.input(),
                                nodes,
                                edges,
                                failurePolicy);
        }
}
