package com.flowforge.engine.registry;

import com.flowforge.domain.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ExecutorRegistry {
    private final Map<String, TaskExecutor> executorsByType;

    public ExecutorRegistry(List<TaskExecutor> executors) {
        this.executorsByType = executors.stream()
                .collect(Collectors.toUnmodifiableMap(TaskExecutor::type, Function.identity()));
    }

    public TaskExecutor resolve(String type) {
        TaskExecutor executor = executorsByType.get(type);
        if (executor == null) {
            throw new UnknownTaskTypeException(type);
        }
        return executor;
    }

}
