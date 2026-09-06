package com.flowforge.temporal;

import com.flowforge.domain.execution.ExecutionState;
import com.flowforge.domain.execution.NodeExecutionState;
import com.flowforge.domain.task.TaskResult;
import com.flowforge.domain.workflow.WorkflowDefinition;

import java.util.Map;

public record ExecutionUpdate(
        String executionId,
        WorkflowDefinition definition,
        Map<String, Object> input,
        ExecutionState state,
        Map<String, NodeExecutionState> nodeStates,
        Map<String, TaskResult> results,
        String failureReason,
        String compensationExecutionId
) {
}
