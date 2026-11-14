package com.nexus.agent.repository;

import com.nexus.agent.entity.AgentApproval;
import com.nexus.agent.entity.AgentApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentApprovalRepository extends JpaRepository<AgentApproval, UUID> {
    Optional<AgentApproval> findByIdAndUserId(UUID id, UUID userId);
    List<AgentApproval> findByUserIdOrderByRequestedAtDesc(UUID userId);
    List<AgentApproval> findByUserIdAndStatusOrderByRequestedAtDesc(UUID userId, AgentApprovalStatus status);
    List<AgentApproval> findByAgentRunId(UUID agentRunId);
}
