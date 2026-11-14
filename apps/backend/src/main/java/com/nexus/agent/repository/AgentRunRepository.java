package com.nexus.agent.repository;

import com.nexus.agent.entity.AgentRun;
import com.nexus.agent.entity.AgentRunStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentRunRepository extends JpaRepository<AgentRun, UUID> {
    Optional<AgentRun> findByIdAndUserId(UUID id, UUID userId);
    Page<AgentRun> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Page<AgentRun> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, AgentRunStatus status, Pageable pageable);
    List<AgentRun> findByUserIdAndAgentIdOrderByCreatedAtDesc(UUID userId, String agentId);
}
