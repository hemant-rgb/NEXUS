package com.nexus.agent.dto;

import com.nexus.agent.entity.AgentStep;
import com.nexus.agent.entity.AgentStepStatus;
import com.nexus.agent.entity.AgentStepType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AgentStepResponse(
        UUID id,
        int sequenceOrder,
        AgentStepType stepType,
        String toolId,
        AgentStepStatus status,
        String inputSummary,
        String outputSummary,
        OffsetDateTime startedAt,
        OffsetDateTime completedAt
) {
    public static AgentStepResponse from(AgentStep step) {
        return new AgentStepResponse(
                step.getId(),
                step.getSequenceOrder(),
                step.getStepType(),
                step.getToolId(),
                step.getStatus(),
                step.getInputSummary(),
                step.getOutputSummary(),
                step.getStartedAt(),
                step.getCompletedAt()
        );
    }
}
