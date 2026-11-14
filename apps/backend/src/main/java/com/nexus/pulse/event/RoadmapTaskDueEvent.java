package com.nexus.pulse.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RoadmapTaskDueEvent(
        String eventId,
        UUID userId,
        UUID roadmapId,
        UUID taskId,
        String taskTitle,
        OffsetDateTime dueDate,
        OffsetDateTime timestamp
) implements NexusDomainEvent {
    public static RoadmapTaskDueEvent of(UUID userId, UUID roadmapId, UUID taskId, String taskTitle, OffsetDateTime dueDate) {
        return new RoadmapTaskDueEvent(
                UUID.randomUUID().toString(),
                userId,
                roadmapId,
                taskId,
                taskTitle,
                dueDate,
                OffsetDateTime.now()
        );
    }

    @Override
    public String getEventId() { return eventId; }

    @Override
    public String getEventType() { return "ROADMAP_TASK_DUE"; }

    @Override
    public UUID getUserId() { return userId; }

    @Override
    public OffsetDateTime getTimestamp() { return timestamp; }
}
