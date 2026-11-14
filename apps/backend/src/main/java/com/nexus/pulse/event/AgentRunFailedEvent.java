package com.nexus.pulse.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AgentRunFailedEvent(
        String eventId,
        UUID userId,
        UUID runId,
        String agentId,
        String errorSummary,
        OffsetDateTime timestamp
) implements NexusDomainEvent {
    public static AgentRunFailedEvent of(UUID userId, UUID runId, String agentId, String errorSummary) {
        return new AgentRunFailedEvent(
                UUID.randomUUID().toString(),
                userId,
                runId,
                agentId,
                errorSummary,
                OffsetDateTime.now()
        );
    }

    @Override
    public String getEventId() { return eventId; }

    @Override
    public String getEventType() { return "AGENT_RUN_FAILED"; }

    @Override
    public UUID getUserId() { return userId; }

    @Override
    public OffsetDateTime getTimestamp() { return timestamp; }
}
