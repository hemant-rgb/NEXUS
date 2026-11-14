package com.nexus.agent.core;

public record AgentActionApprovalRequest(
        String actionType,
        String actionSummary,
        String actionPayload,
        int autonomyLevel
) {}
