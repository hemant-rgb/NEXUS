package com.nexus.agent.repository;

import com.nexus.agent.entity.AgentStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AgentStepRepository extends JpaRepository<AgentStep, UUID> {
    List<AgentStep> findByAgentRunIdOrderBySequenceOrderAsc(UUID agentRunId);
}
