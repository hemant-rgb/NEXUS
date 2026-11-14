package com.nexus.agent.dto;

import com.nexus.agent.entity.AgentRun;
import com.nexus.agent.entity.AgentRunStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record AgentRunResponse(
        UUID id,
        String agentId,
        String workflowId,
        AgentRunStatus status,
        String triggerType,
        String inputSummary,
        String outputSummary,
        String outputResult,
        String errorSummary,
        int tokenUsage,
        BigDecimal estimatedCost,
        OffsetDateTime startedAt,
        OffsetDateTime completedAt,
        OffsetDateTime createdAt,
        List<AgentStepResponse> steps,
        List<AgentApprovalResponse> approvals
) {
    public static AgentRunResponse from(AgentRun r) {
        List<AgentStepResponse> stepDtos = r.getSteps() != null
                ? r.getSteps().stream().map(AgentStepResponse::from).toList()
                : Collections.emptyList();

        List<AgentApprovalResponse> approvalDtos = r.getApprovals() != null
                ? r.getApprovals().stream().map(AgentApprovalResponse::from).toList()
                : Collections.emptyList();

        return new AgentRunResponse(
                r.getId(),
                r.getAgentId(),
                r.getWorkflowId(),
                r.getStatus(),
                r.getTriggerType(),
                r.getInputSummary(),
                r.getOutputSummary(),
                r.getOutputResult(),
                r.getErrorSummary(),
                r.getTokenUsage(),
                r.getEstimatedCost(),
                r.getStartedAt(),
                r.getCompletedAt(),
                r.getCreatedAt(),
                stepDtos,
                approvalDtos
        );
    }
}
