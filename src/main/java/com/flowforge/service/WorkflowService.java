package com.flowforge.service;

import com.flowforge.domain.execution.ExecutionContext;
import com.flowforge.domain.workflow.DagValidator;
import com.flowforge.domain.workflow.WorkflowDefinition;
import com.flowforge.engine.port.WorkflowExecutionPort;
import com.flowforge.persistence.ExecutionRecord;
import com.flowforge.persistence.InMemoryWorkflowStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class WorkflowService {
    private final DagValidator dagValidator = new DagValidator();
    private final InMemoryWorkflowStore store;
    private final WorkflowExecutionPort executionPort;

    public WorkflowService(InMemoryWorkflowStore store, WorkflowExecutionPort executionPort) {
        this.store = store;
        this.executionPort = executionPort;
    }

    public WorkflowDefinition register(WorkflowDefinition definition) {
        dagValidator.validate(definition);
        return store.saveWorkflow(definition);
    }

    public List<WorkflowDefinition> list() {
        return store.listWorkflows();
    }

    public WorkflowDefinition get(String name) {
        return store.findWorkflow(name)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + name));
    }

    public ExecutionRecord execute(String workflowName, Map<String, Object> input) {
        WorkflowDefinition definition = get(workflowName);
        Map<String, Object> executionInput = input == null || input.isEmpty() ? definition.input() : input;
        return executionPort.submit(definition, new ExecutionContext(executionInput));
    }
}
