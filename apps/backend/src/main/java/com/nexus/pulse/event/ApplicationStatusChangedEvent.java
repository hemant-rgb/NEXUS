package com.nexus.pulse.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ApplicationStatusChangedEvent(
        String eventId,
        UUID userId,
        UUID applicationId,
        String companyName,
        String roleTitle,
        String oldStatus,
        String newStatus,
        OffsetDateTime timestamp
) implements NexusDomainEvent {
    public static ApplicationStatusChangedEvent of(UUID userId, UUID applicationId, String companyName, String roleTitle, String oldStatus, String newStatus) {
        return new ApplicationStatusChangedEvent(
                UUID.randomUUID().toString(),
                userId,
                applicationId,
                companyName,
                roleTitle,
                oldStatus,
                newStatus,
                OffsetDateTime.now()
        );
    }

    @Override
    public String getEventId() { return eventId; }

    @Override
    public String getEventType() { return "APPLICATION_STATUS_CHANGED"; }

    @Override
    public UUID getUserId() { return userId; }

    @Override
    public OffsetDateTime getTimestamp() { return timestamp; }
}
