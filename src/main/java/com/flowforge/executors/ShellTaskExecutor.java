package com.flowforge.executors;

import com.flowforge.domain.task.TaskExecutor;
import com.flowforge.domain.task.TaskRequest;
import com.flowforge.domain.task.TaskResult;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ShellTaskExecutor implements TaskExecutor {
    @Override
    public String type() {
        return "shell";
    }

    @Override
    public TaskResult execute(TaskRequest request) {
        String command = String.valueOf(request.config().getOrDefault("command", ""));
        if (command.isBlank()) {
            return TaskResult.failure("shell command is required");
        }
        if (Boolean.TRUE.equals(request.config().get("simulateFailure"))) {
            return TaskResult.failure("simulated shell failure");
        }
        long delayMs = ((Number) request.config().getOrDefault("delayMs", 450)).longValue();
        sleep(delayMs);
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("command", command);
        output.put("exitCode", 0);
        output.put("stdout", "simulated shell execution completed");
        output.putAll(configuredOutput(request.config()));
        return TaskResult.success(output);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> configuredOutput(Map<String, Object> config) {
        Object output = config.get("output");
        return output instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private void sleep(long delayMs) {
        try {
            Thread.sleep(Duration.ofMillis(Math.max(0, delayMs)).toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
