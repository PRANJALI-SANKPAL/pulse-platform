package com.pulse.platform.dto;

import com.pulse.platform.model.WorkflowStatus;

import java.time.Instant;
import java.util.UUID;

public record WorkflowResponse(

        UUID id,
        String name,
        String description,
        WorkflowStatus status,
        Instant createdAt

) {
}