package com.flowforge.domain.task;

public interface TaskExecutor {
    String type();

    TaskResult execute(TaskRequest request);
}
