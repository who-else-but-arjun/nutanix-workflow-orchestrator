package com.flowforge.domain.task;

import java.util.Map;

public record TaskRequest(
                String nodeId,
                String flowId,
                String type,
                Map<String, Object> config,
                Map<String, Object> workflowInput,
                Map<String, Object> previousOutputs) {
}
