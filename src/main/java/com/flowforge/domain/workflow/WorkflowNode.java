package com.flowforge.domain.workflow;

import java.util.Map;

public record WorkflowNode(
        String id,
        String type,
        Map<String, Object> config
) {
    public WorkflowNode {
        config = config == null ? Map.of() : Map.copyOf(config);
    }
}
