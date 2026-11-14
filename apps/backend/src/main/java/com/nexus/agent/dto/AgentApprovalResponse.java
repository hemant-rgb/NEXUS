package com.nexus.agent.dto;

import com.nexus.agent.entity.AgentApproval;
import com.nexus.agent.entity.AgentApprovalStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AgentApprovalResponse(
        UUID id,
        UUID agentRunId,
        String actionType,
        String actionSummary,
        String actionPayload,
        int autonomyLevel,
        AgentApprovalStatus status,
        OffsetDateTime requestedAt,
        OffsetDateTime expiresAt,
        OffsetDateTime decidedAt,
        String decisionReason
) {
    public static AgentApprovalResponse from(AgentApproval a) {
        return new AgentApprovalResponse(
                a.getId(),
                a.getAgentRun().getId(),
                a.getActionType(),
                a.getActionSummary(),
                a.getActionPayload(),
                a.getAutonomyLevel(),
                a.getStatus(),
                a.getRequestedAt(),
                a.getExpiresAt(),
                a.getDecidedAt(),
                a.getDecisionReason()
        );
    }
}
