package com.pulse.platform.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;
import java.util.concurrent.RejectedExecutionException;

import java.util.UUID;
import java.util.concurrent.Executor;

@Service
public class ExecutionQueueService {

    private final Executor executionTaskExecutor;
    private final ExecutionWorkerService executionWorkerService;

    public ExecutionQueueService(
            @Qualifier("executionTaskExecutor") Executor executionTaskExecutor,
            ExecutionWorkerService executionWorkerService) {
        this.executionTaskExecutor = executionTaskExecutor;
        this.executionWorkerService = executionWorkerService;
    }

    public void submit(UUID executionId) {
        try {
            executionTaskExecutor.execute(
                    () -> executionWorkerService.processExecution(executionId)
            );
        } catch (RejectedExecutionException exception) {
            throw new IllegalStateException(
                    "Execution queue is full. Please try again later.",
                    exception
            );
        }
    }
}