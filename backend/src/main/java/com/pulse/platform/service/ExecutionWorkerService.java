package com.pulse.platform.service;

import com.pulse.platform.model.Execution;
import com.pulse.platform.repository.ExecutionRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ExecutionWorkerService {

    private static final Logger log =
            LoggerFactory.getLogger(ExecutionWorkerService.class);

    private static final int MAX_RETRIES = 3;

    private final ExecutionRepository executionRepository;
    private final MeterRegistry meterRegistry;

    @Value("${pulse.worker.simulate-first-attempt-failure:false}")
    private boolean simulateFirstAttemptFailure;

    public ExecutionWorkerService(
            ExecutionRepository executionRepository,
            MeterRegistry meterRegistry) {
        this.executionRepository = executionRepository;
        this.meterRegistry = meterRegistry;
    }

    public void processExecution(UUID executionId) {
        int claimed =
                executionRepository.claimQueuedExecution(executionId);

        if (claimed == 0) {
            log.info("Execution claim skipped: executionId={}", executionId);
            meterRegistry.counter("pulse.execution.claims",
                    "result", "skipped").increment();
            return;
        }

        log.info("Execution started: executionId={}", executionId);
        meterRegistry.counter("pulse.execution.started").increment();

        Execution execution = executionRepository.findById(executionId)
                .orElse(null);

        if (execution == null) {
            log.error("Claimed execution not found: executionId={}",
                    executionId);
            meterRegistry.counter("pulse.execution.errors",
                    "type", "missing_record").increment();
            return;
        }

        while (true) {
            try {
                Thread.sleep(2000);

                if (simulateFirstAttemptFailure
                        && execution.getRetryCount() == 0) {
                    throw new IllegalStateException(
                            "Simulated temporary failure");
                }

                execution.markCompleted();
                executionRepository.save(execution);

                log.info("Execution completed: executionId={}, retries={}",
                        executionId, execution.getRetryCount());

                meterRegistry.counter("pulse.execution.finished",
                        "outcome", "completed").increment();
                return;

            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                fail(execution, "Execution interrupted");
                return;

            } catch (Exception exception) {
                String message = exception.getMessage() == null
                        ? "Unknown execution error"
                        : exception.getMessage();

                log.warn("Execution attempt failed: executionId={}, "
                                + "retries={}, error={}",
                        executionId, execution.getRetryCount(), message);

                if (execution.getRetryCount() >= MAX_RETRIES) {
                    fail(execution, "Retry limit reached: " + message);
                    return;
                }

                execution.markRetrying(message);
                executionRepository.save(execution);

                meterRegistry.counter("pulse.execution.retries").increment();

                long backoffMillis =
                        1000L * execution.getRetryCount();

                try {
                    Thread.sleep(backoffMillis);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    fail(execution, "Interrupted during retry backoff");
                    return;
                }

                execution.markRunning();
                executionRepository.save(execution);
            }
        }
    }

    private void fail(Execution execution, String message) {
        execution.markFailed(message);
        executionRepository.save(execution);

        log.error("Execution failed: executionId={}, error={}",
                execution.getId(), message);

        meterRegistry.counter("pulse.execution.finished",
                "outcome", "failed").increment();
    }
}