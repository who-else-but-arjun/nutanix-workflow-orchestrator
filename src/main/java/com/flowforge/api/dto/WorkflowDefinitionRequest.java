package com.flowforge.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

public record WorkflowDefinitionRequest(
                @NotBlank String workflowName,
                String flowId,
                String version,
                Map<String, Object> input,
                @NotEmpty List<NodeRequest> nodes,
                List<List<String>> edges,
                FailurePolicyRequest onFailure) {
        public WorkflowDefinitionRequest(
                        String workflowName,
                        String version,
                        Map<String, Object> input,
                        List<NodeRequest> nodes,
                        List<List<String>> edges,
                        FailurePolicyRequest onFailure) {
                this(workflowName, workflowName, version, input, nodes, edges, onFailure);
        }
}
