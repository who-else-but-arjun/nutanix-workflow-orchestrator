package com.flowforge.domain.execution;

public enum ExecutionState {
    PENDING,
    RUNNING,
    SUCCEEDED,
    FAILED,
    COMPENSATING,
    COMPENSATED,
    COMPENSATION_FAILED
}
