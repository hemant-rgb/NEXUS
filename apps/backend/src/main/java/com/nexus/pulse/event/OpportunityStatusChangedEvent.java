package com.nexus.pulse.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OpportunityStatusChangedEvent(
        String eventId,
        UUID userId,
        UUID opportunityId,
        String title,
        String oldStatus,
        String newStatus,
        OffsetDateTime timestamp
) implements NexusDomainEvent {
    public static OpportunityStatusChangedEvent of(UUID userId, UUID opportunityId, String title, String oldStatus, String newStatus) {
        return new OpportunityStatusChangedEvent(
                UUID.randomUUID().toString(),
                userId,
                opportunityId,
                title,
                oldStatus,
                newStatus,
                OffsetDateTime.now()
        );
    }

    @Override
    public String getEventId() { return eventId; }

    @Override
    public String getEventType() { return "OPPORTUNITY_STATUS_CHANGED"; }

    @Override
    public UUID getUserId() { return userId; }

    @Override
    public OffsetDateTime getTimestamp() { return timestamp; }
}
