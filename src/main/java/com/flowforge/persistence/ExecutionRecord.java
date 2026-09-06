package com.flowforge.persistence;

import com.flowforge.domain.execution.ExecutionState;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ExecutionRecord(
        String id,
        String workflowName,
        String version,
        ExecutionState state,
        Instant startedAt,
        Instant finishedAt,
        Map<String, Object> input,
        List<ExecutionNodeRecord> nodes,
        String failureReason,
        String compensationExecutionId,
        String temporalWorkflowId,
        String temporalRunId,
        String temporalNamespace,
        String temporalTaskQueue
) {
    public long durationMillis() {
        Instant end = finishedAt == null ? Instant.now() : finishedAt;
        return startedAt == null ? 0 : java.time.Duration.between(startedAt, end).toMillis();
    }
}
