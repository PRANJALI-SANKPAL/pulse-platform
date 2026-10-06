package com.pulse.platform.service;

import com.pulse.platform.dto.CreateWorkflowRequest;
import com.pulse.platform.dto.WorkflowResponse;
import com.pulse.platform.model.Workflow;
import com.pulse.platform.repository.WorkflowRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WorkflowService {

    private final WorkflowRepository workflowRepository;

    public WorkflowService(WorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    public WorkflowResponse createWorkflow(CreateWorkflowRequest request) {

        Workflow workflow = new Workflow(
                request.name(),
                request.description()
        );

        Workflow savedWorkflow = workflowRepository.save(workflow);

        return toResponse(savedWorkflow);
    }

    public WorkflowResponse getWorkflow(UUID workflowId) {

        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() ->
                        new RuntimeException("Workflow not found: " + workflowId)
                );

        return toResponse(workflow);
    }

    private WorkflowResponse toResponse(Workflow workflow) {

        return new WorkflowResponse(
                workflow.getId(),
                workflow.getName(),
                workflow.getDescription(),
                workflow.getStatus(),
                workflow.getCreatedAt()
        );
    }
}

























//package com.pulse.platform.service;
//
//import com.pulse.platform.dto.CreateWorkflowRequest;
//import com.pulse.platform.dto.WorkflowResponse;
//import com.pulse.platform.model.WorkflowStatus;
//import org.springframework.stereotype.Service;
//
//import java.time.Instant;
//import java.util.UUID;
//
//@Service
//public class WorkflowService {
//
//    public WorkflowResponse createWorkflow(CreateWorkflowRequest request) {
//
//        return new WorkflowResponse(
//                UUID.randomUUID(),
//                request.name(),
//                request.description(),
//                WorkflowStatus.ACTIVE,
//                Instant.now()
//        );
//    }
//}