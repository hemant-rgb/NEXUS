package com.nexus.pulse.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AgentRunCompletedEvent(
        String eventId,
        UUID userId,
        UUID runId,
        String agentId,
        String summary,
        OffsetDateTime timestamp
) implements NexusDomainEvent {
    public static AgentRunCompletedEvent of(UUID userId, UUID runId, String agentId, String summary) {
        return new AgentRunCompletedEvent(
                UUID.randomUUID().toString(),
                userId,
                runId,
                agentId,
                summary,
                OffsetDateTime.now()
        );
    }

    @Override
    public String getEventId() { return eventId; }

    @Override
    public String getEventType() { return "AGENT_RUN_COMPLETED"; }

    @Override
    public UUID getUserId() { return userId; }

    @Override
    public OffsetDateTime getTimestamp() { return timestamp; }
}
