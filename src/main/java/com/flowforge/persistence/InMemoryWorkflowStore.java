package com.flowforge.persistence;

import com.flowforge.domain.workflow.WorkflowDefinition;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryWorkflowStore {
    private final ConcurrentHashMap<String, WorkflowDefinition> workflows = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ExecutionRecord> executions = new ConcurrentHashMap<>();

    public WorkflowDefinition saveWorkflow(WorkflowDefinition definition) {
        workflows.put(definition.workflowName(), definition);
        return definition;
    }

    public List<WorkflowDefinition> listWorkflows() {
        return workflows.values().stream()
                .sorted(Comparator.comparing(WorkflowDefinition::workflowName))
                .toList();
    }

    public Optional<WorkflowDefinition> findWorkflow(String name) {
        return Optional.ofNullable(workflows.get(name));
    }

    public ExecutionRecord saveExecution(ExecutionRecord record) {
        executions.put(record.id(), record);
        return record;
    }

    public Optional<ExecutionRecord> findExecution(String id) {
        return Optional.ofNullable(executions.get(id));
    }

    public List<ExecutionRecord> listExecutions() {
        return executions.values().stream()
                .sorted(Comparator.comparing(ExecutionRecord::startedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }
}
