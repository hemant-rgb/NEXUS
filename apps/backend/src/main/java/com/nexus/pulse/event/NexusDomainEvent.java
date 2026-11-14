package com.nexus.pulse.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface NexusDomainEvent {
    String getEventId();
    String getEventType();
    UUID getUserId();
    OffsetDateTime getTimestamp();
}
