package com.flowforge.engine.flow;

import com.flowforge.domain.task.TaskRequest;
import com.flowforge.domain.task.TaskResult;

public interface WorkflowFlow {
    String flowId();

    TaskResult execute(TaskRequest request);
}
