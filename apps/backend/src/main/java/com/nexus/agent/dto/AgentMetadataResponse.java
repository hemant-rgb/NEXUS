package com.nexus.agent.dto;

import com.nexus.agent.core.NexusAgent;

import java.util.List;

public record AgentMetadataResponse(
        String agentId,
        String name,
        String description,
        int autonomyLevel,
        String autonomyDescription,
        List<String> permittedTools
) {
    public static AgentMetadataResponse from(NexusAgent agent) {
        return new AgentMetadataResponse(
                agent.getAgentId(),
                agent.getName(),
                agent.getDescription(),
                agent.getAutonomyLevel().getLevel(),
                agent.getAutonomyLevel().getDescription(),
                agent.getPermittedTools()
        );
    }
}
