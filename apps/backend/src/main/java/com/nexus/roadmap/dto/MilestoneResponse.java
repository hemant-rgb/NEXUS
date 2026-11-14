package com.nexus.roadmap.dto;

import com.nexus.roadmap.entity.MilestoneStatus;
import com.nexus.roadmap.entity.RoadmapMilestone;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record MilestoneResponse(
        UUID id,
        UUID roadmapId,
        String title,
        String description,
        Integer sequenceOrder,
        Integer estimatedDurationWeeks,
        LocalDate targetDate,
        MilestoneStatus status,
        Double completionPercentage,
        List<TaskResponse> tasks
) {
    public static MilestoneResponse from(RoadmapMilestone m) {
        if (m == null) return null;
        List<TaskResponse> taskResponses = m.getTasks() != null
                ? m.getTasks().stream().map(TaskResponse::from).toList()
                : Collections.emptyList();

        return new MilestoneResponse(
                m.getId(),
                m.getRoadmap() != null ? m.getRoadmap().getId() : null,
                m.getTitle(),
                m.getDescription(),
                m.getSequenceOrder(),
                m.getEstimatedDurationWeeks(),
                m.getTargetDate(),
                m.getStatus(),
                m.getCompletionPercentage(),
                taskResponses
        );
    }
}
