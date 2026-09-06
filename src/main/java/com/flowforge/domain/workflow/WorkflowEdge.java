package com.flowforge.domain.workflow;

public record WorkflowEdge(String from, String to, String condition) {
    public WorkflowEdge(String from, String to) {
        this(from, to, null);
    }

    public boolean hasCondition() {
        return condition != null && !condition.isBlank();
    }
}
