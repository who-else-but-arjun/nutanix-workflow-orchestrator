package com.flowforge.domain.workflow;

public record FailurePolicy(String compensationFlow) {
    public boolean hasCompensation() {
        return compensationFlow != null && !compensationFlow.isBlank();
    }
}
