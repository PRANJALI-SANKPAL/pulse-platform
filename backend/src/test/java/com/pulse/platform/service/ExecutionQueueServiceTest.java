package com.pulse.platform.service;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class ExecutionQueueServiceTest {

    @Test
    void submitShouldScheduleExecutionForProcessing() {
        ExecutionWorkerService worker = mock(ExecutionWorkerService.class);
        UUID executionId = UUID.randomUUID();

        Executor directExecutor = Runnable::run;

        ExecutionQueueService queueService =
                new ExecutionQueueService(directExecutor, worker);

        assertDoesNotThrow(() -> queueService.submit(executionId));

        verify(worker).processExecution(executionId);
    }

    @Test
    void submitShouldReportWhenQueueRejectsTask() {
        ExecutionWorkerService worker = mock(ExecutionWorkerService.class);

        Executor rejectingExecutor = task -> {
            throw new java.util.concurrent.RejectedExecutionException(
                    "Queue is full");
        };

        ExecutionQueueService queueService =
                new ExecutionQueueService(rejectingExecutor, worker);

        UUID executionId = UUID.randomUUID();

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> queueService.submit(executionId)
        );

        verifyNoInteractions(worker);
    }
}