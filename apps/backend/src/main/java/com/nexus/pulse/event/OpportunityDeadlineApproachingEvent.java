package com.nexus.pulse.event;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OpportunityDeadlineApproachingEvent(
        String eventId,
        UUID userId,
        UUID opportunityId,
        String title,
        LocalDate deadline,
        long daysRemaining,
        OffsetDateTime timestamp
) implements NexusDomainEvent {
    public static OpportunityDeadlineApproachingEvent of(UUID userId, UUID opportunityId, String title, LocalDate deadline, long daysRemaining) {
        return new OpportunityDeadlineApproachingEvent(
                UUID.randomUUID().toString(),
                userId,
                opportunityId,
                title,
                deadline,
                daysRemaining,
                OffsetDateTime.now()
        );
    }

    @Override
    public String getEventId() { return eventId; }

    @Override
    public String getEventType() { return "OPPORTUNITY_DEADLINE_APPROACHING"; }

    @Override
    public UUID getUserId() { return userId; }

    @Override
    public OffsetDateTime getTimestamp() { return timestamp; }
}
