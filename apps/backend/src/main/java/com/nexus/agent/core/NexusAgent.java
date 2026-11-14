package com.nexus.agent.core;

import com.nexus.agent.entity.AgentAutonomyLevel;

import java.util.List;

public interface NexusAgent {
    String getAgentId();
    String getName();
    String getDescription();
    AgentAutonomyLevel getAutonomyLevel();
    List<String> getPermittedTools();
    AgentExecutionResult execute(AgentContext context);
}
