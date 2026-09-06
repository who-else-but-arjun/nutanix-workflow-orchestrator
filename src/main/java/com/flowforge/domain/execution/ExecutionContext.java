package com.flowforge.domain.execution;

import java.util.Map;

public class ExecutionContext {
    private final Map<String, Object> workflowInput;

    public ExecutionContext(Map<String, Object> workflowInput) {
        this.workflowInput = workflowInput == null ? Map.of() : Map.copyOf(workflowInput);
    }

    public Map<String, Object> workflowInput() {
        return workflowInput;
    }
}
