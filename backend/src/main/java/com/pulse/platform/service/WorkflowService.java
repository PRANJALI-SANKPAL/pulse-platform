package com.pulse.platform.service;

import com.pulse.platform.dto.CreateWorkflowRequest;
import com.pulse.platform.dto.WorkflowResponse;
import com.pulse.platform.model.WorkflowStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class WorkflowService {

    public WorkflowResponse createWorkflow(CreateWorkflowRequest request) {

        return new WorkflowResponse(
                UUID.randomUUID(),
                request.name(),
                request.description(),
                WorkflowStatus.ACTIVE,
                Instant.now()
        );
    }
}