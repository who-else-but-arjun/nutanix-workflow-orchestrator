package com.flowforge.service;

import com.flowforge.persistence.ExecutionRecord;
import com.flowforge.persistence.InMemoryWorkflowStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExecutionQueryService {
    private final InMemoryWorkflowStore store;

    public ExecutionQueryService(InMemoryWorkflowStore store) {
        this.store = store;
    }

    public List<ExecutionRecord> list() {
        return store.listExecutions();
    }

    public ExecutionRecord get(String id) {
        return store.findExecution(id)
                .orElseThrow(() -> new IllegalArgumentException("Execution not found: " + id));
    }
}
