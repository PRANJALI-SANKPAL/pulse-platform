package com.pulse.platform.service;

import com.pulse.platform.dto.ExecutionResponse;
import com.pulse.platform.model.Execution;
import com.pulse.platform.model.Workflow;
import com.pulse.platform.repository.ExecutionRepository;
import com.pulse.platform.repository.WorkflowRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ExecutionService {

    private final ExecutionRepository executionRepository;
    private final WorkflowRepository workflowRepository;

    public ExecutionService(
            ExecutionRepository executionRepository,
            WorkflowRepository workflowRepository) {

        this.executionRepository = executionRepository;
        this.workflowRepository = workflowRepository;
    }

    public ExecutionResponse createExecution(UUID workflowId) {

        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Workflow not found: " + workflowId
                        )
                );

        Execution execution = new Execution(workflow);

        /*
         * For now we immediately move the execution
         * to QUEUED.
         *
         * Kafka will actually handle asynchronous
         * processing from Day 7.
         */
        execution.markQueued();

        Execution savedExecution =
                executionRepository.save(execution);

        return toResponse(savedExecution);
    }

    public ExecutionResponse getExecution(UUID executionId) {

        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() ->
                        new RuntimeException(
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