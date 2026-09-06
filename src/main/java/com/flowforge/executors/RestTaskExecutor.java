package com.flowforge.executors;

import com.flowforge.domain.task.TaskExecutor;
import com.flowforge.domain.task.TaskRequest;
import com.flowforge.domain.task.TaskResult;
import com.flowforge.engine.flow.WorkflowFlowRegistry;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RestTaskExecutor implements TaskExecutor {
    private final WorkflowFlowRegistry flowRegistry;

    public RestTaskExecutor(WorkflowFlowRegistry flowRegistry) {
        this.flowRegistry = flowRegistry;
    }

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
        TaskResult flowResult = flowRegistry.resolve(request.flowId()).execute(request);
        if (!flowResult.success()) {
            return flowResult;
        }
        Map<String, Object> output = new LinkedHashMap<>();
        output.putAll(DummyApiClient.call(method, url));
        output.putAll(flowResult.output());
        output.putAll(configuredOutput(request.config()));
        return TaskResult.success(output);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> configuredOutput(Map<String, Object> config) {
        Object output = config.get("output");
        return output instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

}
