package com.flowforge.engine.port;

import com.flowforge.domain.execution.ExecutionContext;
import com.flowforge.domain.workflow.WorkflowDefinition;
import com.flowforge.persistence.ExecutionRecord;

import java.util.Optional;

public interface WorkflowExecutionPort {
    ExecutionRecord submit(WorkflowDefinition definition, ExecutionContext context);

    Optional<ExecutionRecord> getStatus(String executionId);
}
