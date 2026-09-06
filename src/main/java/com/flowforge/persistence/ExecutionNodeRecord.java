package com.flowforge.persistence;

import com.flowforge.domain.execution.NodeExecutionState;

import java.time.Instant;
import java.util.Map;

public record ExecutionNodeRecord(
        String nodeId,
        String type,
        NodeExecutionState state,
        Instant startedAt,
        Instant finishedAt,
        Map<String, Object> output,
        String error
) {
}
