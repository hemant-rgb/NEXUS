package com.nexus.pulse.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InterviewScheduledEvent(
        String eventId,
        UUID userId,
        UUID applicationId,
        String companyName,
        String roundType,
        OffsetDateTime scheduledAt,
        OffsetDateTime timestamp
) implements NexusDomainEvent {
    public static InterviewScheduledEvent of(UUID userId, UUID applicationId, String companyName, String roundType, OffsetDateTime scheduledAt) {
        return new InterviewScheduledEvent(
                UUID.randomUUID().toString(),
                userId,
                applicationId,
                companyName,
                roundType,
                scheduledAt,
                OffsetDateTime.now()
        );
    }

    @Override
    public String getEventId() { return eventId; }

    @Override
    public String getEventType() { return "INTERVIEW_SCHEDULED"; }

    @Override
    public UUID getUserId() { return userId; }

    @Override
    public OffsetDateTime getTimestamp() { return timestamp; }
}
