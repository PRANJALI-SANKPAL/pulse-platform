package com.pulse.platform.controller;

import com.pulse.platform.dto.CreateWorkflowRequest;
import com.pulse.platform.dto.WorkflowResponse;
import com.pulse.platform.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkflowResponse createWorkflow(
            @Valid @RequestBody CreateWorkflowRequest request) {

        return workflowService.createWorkflow(request);
    }
}