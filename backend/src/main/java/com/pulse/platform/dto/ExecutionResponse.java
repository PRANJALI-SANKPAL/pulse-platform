package com.pulse.platform.dto;

import com.pulse.platform.model.ExecutionStatus;

import java.time.Instant;
import java.util.UUID;

public record ExecutionResponse(

        UUID id,
        UUID workflowId,
        ExecutionStatus status,
        Instant createdAt,
        Instant startedAt,
        Instant completedAt,
        String errorMessage,
        int retryCount

) {
}