package com.flowforge.executors;

import com.flowforge.domain.task.TaskExecutor;
import com.flowforge.domain.task.TaskRequest;
import com.flowforge.domain.task.TaskResult;
import com.flowforge.engine.flow.WorkflowFlowRegistry;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ShellTaskExecutor implements TaskExecutor {
    private final WorkflowFlowRegistry flowRegistry;

    public ShellTaskExecutor(WorkflowFlowRegistry flowRegistry) {
        this.flowRegistry = flowRegistry;
    }

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
        TaskResult flowResult = flowRegistry.resolve(request.flowId()).execute(request);
        if (!flowResult.success()) {
            return flowResult;
        }
        Map<String, Object> shellResult = DummyShellRunner.run(command);
        if (!Integer.valueOf(0).equals(shellResult.get("exitCode"))) {
            return TaskResult.failure(String.valueOf(shellResult.getOrDefault("error", "shell task failed")));
        }
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("command", command);
        output.putAll(shellResult);
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
