package com.nexus.roadmap.dto;

import com.nexus.roadmap.entity.RoadmapTask;
import com.nexus.roadmap.entity.TaskPriority;
import com.nexus.roadmap.entity.TaskStatus;
import com.nexus.roadmap.entity.TaskType;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        UUID milestoneId,
        UUID roadmapId,
        String title,
        String description,
        String completionCriteria,
        TaskType taskType,
        TaskPriority priority,
        Double estimatedHours,
        Integer sequenceOrder,
        LocalDate scheduledDate,
        TaskStatus status,
        OffsetDateTime completedAt,
        String userNotes,
        String dependencies,
        List<TaskResourceResponse> resources
) {
    public static TaskResponse from(RoadmapTask t) {
        if (t == null) return null;
        List<TaskResourceResponse> resList = t.getResources() != null
                ? t.getResources().stream().map(TaskResourceResponse::from).toList()
                : Collections.emptyList();

        return new TaskResponse(
                t.getId(),
                t.getMilestone() != null ? t.getMilestone().getId() : null,
                t.getRoadmap() != null ? t.getRoadmap().getId() : null,
                t.getTitle(),
                t.getDescription(),
                t.getCompletionCriteria(),
                t.getTaskType(),
                t.getPriority(),
                t.getEstimatedHours(),
                t.getSequenceOrder(),
                t.getScheduledDate(),
                t.getStatus(),
                t.getCompletedAt(),
                t.getUserNotes(),
                t.getDependencies(),
                resList
        );
    }
}
