package com.flowforge.domain.task;

import java.util.Map;

public record TaskResult(boolean success, Map<String, Object> output, String error) {
    public static TaskResult success(Map<String, Object> output) {
        return new TaskResult(true, output == null ? Map.of() : Map.copyOf(output), null);
    }

    public static TaskResult failure(String error) {
        return new TaskResult(false, Map.of(), error);
    }
}
