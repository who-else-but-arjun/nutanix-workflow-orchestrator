package com.flowforge.api;

import com.flowforge.persistence.ExecutionRecord;
import com.flowforge.service.ExecutionQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/executions")
public class ExecutionController {
    private final ExecutionQueryService executionQueryService;

    public ExecutionController(ExecutionQueryService executionQueryService) {
        this.executionQueryService = executionQueryService;
    }

    @GetMapping
    public List<ExecutionRecord> list() {
        return executionQueryService.list();
    }

    @GetMapping("/{id}")
    public ExecutionRecord get(@PathVariable("id") String id) {
        return executionQueryService.get(id);
    }
}
