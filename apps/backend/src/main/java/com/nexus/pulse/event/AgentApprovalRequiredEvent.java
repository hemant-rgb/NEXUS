package com.nexus.pulse.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AgentApprovalRequiredEvent(
        String eventId,
        UUID userId,
        UUID runId,
        UUID approvalId,
        String actionType,
        String actionSummary,
        OffsetDateTime timestamp
) implements NexusDomainEvent {
    public static AgentApprovalRequiredEvent of(UUID userId, UUID runId, UUID approvalId, String actionType, String actionSummary) {
        return new AgentApprovalRequiredEvent(
                UUID.randomUUID().toString(),
                userId,
                runId,
                approvalId,
                actionType,
                actionSummary,
                OffsetDateTime.now()
        );
    }

    @Override
    public String getEventId() { return eventId; }

    @Override
    public String getEventType() { return "AGENT_APPROVAL_REQUIRED"; }

    @Override
    public UUID getUserId() { return userId; }

    @Override
    public OffsetDateTime getTimestamp() { return timestamp; }
}
