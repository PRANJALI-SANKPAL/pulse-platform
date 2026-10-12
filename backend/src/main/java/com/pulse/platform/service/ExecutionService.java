package com.pulse.platform.service;

import com.pulse.platform.dto.ExecutionResponse;
import com.pulse.platform.model.Execution;
import com.pulse.platform.model.Workflow;
import com.pulse.platform.repository.ExecutionRepository;
import com.pulse.platform.repository.WorkflowRepository;
import org.springframework.stereotype.Service;
import com.pulse.platform.exception.ResourceNotFoundException;

import java.util.UUID;

@Service
public class ExecutionService {

    private final ExecutionRepository executionRepository;
    private final WorkflowRepository workflowRepository;
    private final ExecutionQueueService executionQueueService;

    public ExecutionService(
            ExecutionRepository executionRepository,
            WorkflowRepository workflowRepository,
            ExecutionQueueService executionQueueService) {
        this.executionRepository = executionRepository;
        this.workflowRepository = workflowRepository;
        this.executionQueueService = executionQueueService;
    }

    public ExecutionResponse createExecution(UUID workflowId) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Workflow not found: " + workflowId
                        )
                );

        Execution execution = new Execution(workflow);
        execution.markQueued();

        Execution savedExecution = executionRepository.save(execution);

        try {
            executionQueueService.submit(savedExecution.getId());
        } catch (IllegalStateException exception) {
            savedExecution.markFailed(exception.getMessage());
            executionRepository.save(savedExecution);
            throw exception;
        }

        return toResponse(savedExecution);
    }

    public ExecutionResponse getExecution(UUID executionId) {
        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Execution not found: " + executionId
                        )
                );

        return toResponse(execution);
    }

    private ExecutionResponse toResponse(Execution execution) {
        return new ExecutionResponse(
                execution.getId(),
                execution.getWorkflow().getId(),
                execution.getStatus(),
                execution.getCreatedAt(),
                execution.getStartedAt(),
                execution.getCompletedAt(),
                execution.getErrorMessage(),
                execution.getRetryCount()
        );
    }
}