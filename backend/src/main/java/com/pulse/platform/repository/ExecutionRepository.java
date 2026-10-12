package com.pulse.platform.repository;

import com.pulse.platform.model.Execution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface ExecutionRepository
        extends JpaRepository<Execution, UUID> {

    @Modifying
    @Transactional
    @Query("""
        UPDATE Execution e
        SET e.status = com.pulse.platform.model.ExecutionStatus.RUNNING,
            e.startedAt = CURRENT_TIMESTAMP
        WHERE e.id = :executionId
          AND e.status = com.pulse.platform.model.ExecutionStatus.QUEUED
        """)
    int claimQueuedExecution(
            @Param("executionId") UUID executionId
    );
}