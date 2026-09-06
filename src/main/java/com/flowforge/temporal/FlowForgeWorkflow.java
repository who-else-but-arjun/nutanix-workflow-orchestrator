package com.flowforge.temporal;

import com.flowforge.domain.execution.ExecutionState;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface FlowForgeWorkflow {
    @WorkflowMethod
    ExecutionState run(TemporalWorkflowRequest request);
}
