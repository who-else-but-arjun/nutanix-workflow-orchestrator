package com.flowforge.temporal;

import com.flowforge.domain.task.TaskRequest;
import com.flowforge.domain.task.TaskResult;
import com.flowforge.domain.workflow.WorkflowDefinition;
import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface FlowForgeActivities {
    TaskResult execute(TaskRequest request);

    void publish(ExecutionUpdate update);

    WorkflowDefinition loadWorkflow(String name);
}
