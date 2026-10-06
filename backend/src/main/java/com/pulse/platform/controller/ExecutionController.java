package com.pulse.platform.controller;

import com.pulse.platform.dto.ExecutionResponse;
import com.pulse.platform.service.ExecutionService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ExecutionController {

    private final ExecutionService executionService;

    public ExecutionController(
            ExecutionService executionService) {

        this.executionService = executionService;
    }

    @PostMapping("/workflows/{workflowId}/execute")
    public ExecutionResponse executeWorkflow(
            @PathVariable UUID workflowId) {

        return executionService.createExecution(workflowId);
    }

    @GetMapping("/executions/{executionId}")
    public ExecutionResponse getExecution(
            @PathVariable UUID executionId) {

        return executionService.getExecution(executionId);
    }
}