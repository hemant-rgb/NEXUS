package com.nexus.pulse.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RoadmapTaskOverdueEvent(
        String eventId,
        UUID userId,
        UUID roadmapId,
        UUID taskId,
        String taskTitle,
        OffsetDateTime dueDate,
        long daysOverdue,
        OffsetDateTime timestamp
) implements NexusDomainEvent {
    public static RoadmapTaskOverdueEvent of(UUID userId, UUID roadmapId, UUID taskId, String taskTitle, OffsetDateTime dueDate, long daysOverdue) {
        return new RoadmapTaskOverdueEvent(
                UUID.randomUUID().toString(),
                userId,
                roadmapId,
                taskId,
                taskTitle,
                dueDate,
                daysOverdue,
                OffsetDateTime.now()
        );
    }

    @Override
    public String getEventId() { return eventId; }

    @Override
    public String getEventType() { return "ROADMAP_TASK_OVERDUE"; }

    @Override
    public UUID getUserId() { return userId; }

    @Override
    public OffsetDateTime getTimestamp() { return timestamp; }
}
