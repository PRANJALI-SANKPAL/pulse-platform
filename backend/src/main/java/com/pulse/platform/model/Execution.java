package com.pulse.platform.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "executions")
public class Execution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExecutionStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant startedAt;

    private Instant completedAt;

    @Column(length = 2000)
    private String errorMessage;

    @Column(nullable = false)
    private int retryCount;

    protected Execution() {
    }

    public Execution(Workflow workflow) {
        this.workflow = workflow;
        this.status = ExecutionStatus.CREATED;
        this.createdAt = Instant.now();
        this.retryCount = 0;
    }

    public UUID getId() {
        return id;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void markQueued() {
        this.status = ExecutionStatus.QUEUED;
    }

    public void markRunning() {
        this.status = ExecutionStatus.RUNNING;
        this.startedAt = Instant.now();
    }

    public void markCompleted() {
        this.status = ExecutionStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status = ExecutionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = Instant.now();
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }
}