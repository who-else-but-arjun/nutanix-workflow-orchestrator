package com.flowforge.api;

import com.flowforge.api.dto.ExecuteWorkflowRequest;
import com.flowforge.api.dto.WorkflowDefinitionRequest;
import com.flowforge.api.dto.WorkflowMapper;
import com.flowforge.domain.workflow.WorkflowDefinition;
import com.flowforge.persistence.ExecutionRecord;
import com.flowforge.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping
    public WorkflowDefinition register(@Valid @RequestBody WorkflowDefinitionRequest request) {
        return workflowService.register(WorkflowMapper.toDomain(request));
    }

    @GetMapping
    public List<WorkflowDefinition> list() {
        return workflowService.list();
    }

    @GetMapping("/{name}")
    public WorkflowDefinition get(@PathVariable("name") String name) {
        return workflowService.get(name);
    }

    @PostMapping("/{name}/execute")
    public ExecutionRecord execute(@PathVariable("name") String name, @RequestBody(required = false) ExecuteWorkflowRequest request) {
        return workflowService.execute(name, request == null ? null : request.input());
    }
}
