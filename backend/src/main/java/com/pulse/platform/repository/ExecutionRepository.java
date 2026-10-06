package com.pulse.platform.repository;

import com.pulse.platform.model.Execution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExecutionRepository extends JpaRepository<Execution, UUID> {
}