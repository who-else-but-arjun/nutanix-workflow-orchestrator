package com.flowforge.executors;

import com.flowforge.domain.task.TaskExecutor;
import com.flowforge.domain.task.TaskRequest;
import com.flowforge.domain.task.TaskResult;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.time.Duration;
import java.util.Map;

@Component
public class RestTaskExecutor implements TaskExecutor {
    @Override
    public String type() {
        return "rest";
    }

    @Override
    public TaskResult execute(TaskRequest request) {
        String method = String.valueOf(request.config().getOrDefault("method", "GET"));
        String url = String.valueOf(request.config().getOrDefault("url", ""));
        if (url.isBlank()) {
            return TaskResult.failure("rest url is required");
        }
        if (Boolean.TRUE.equals(request.config().get("simulateFailure"))) {
            return TaskResult.failure("simulated REST failure at " + url);
        }
        long delayMs = ((Number) request.config().getOrDefault("delayMs", 500)).longValue();
        sleep(delayMs);
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("method", method);
        output.put("url", url);
        output.put("status", 200);
        output.put("body", "simulated REST response accepted");
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
