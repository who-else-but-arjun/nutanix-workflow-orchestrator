package com.flowforge.temporal;

import com.flowforge.domain.workflow.WorkflowDefinition;

import java.util.Map;

public record TemporalWorkflowRequest(
        String executionId,
        WorkflowDefinition definition,
        Map<String, Object> input
) {
}
