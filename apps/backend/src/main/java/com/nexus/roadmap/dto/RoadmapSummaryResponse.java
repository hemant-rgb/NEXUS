package com.nexus.roadmap.dto;

import com.nexus.roadmap.entity.Roadmap;
import com.nexus.roadmap.entity.RoadmapStatus;
import com.nexus.roadmap.entity.RoadmapType;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RoadmapSummaryResponse(
        UUID id,
        String title,
        String description,
        RoadmapType roadmapType,
        UUID targetOpportunityId,
        String targetOpportunityTitle,
        String targetGoal,
        RoadmapStatus status,
        LocalDate startDate,
        LocalDate targetCompletionDate,
        Integer estimatedDurationWeeks,
        Integer availableHoursPerWeek,
        Double overallProgressPercentage,
        int totalMilestones,
        int completedMilestones,
        int totalTasks,
        int completedTasks,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static RoadmapSummaryResponse from(Roadmap r, Double progress, int completedMilestones, int totalTasks, int completedTasks) {
        if (r == null) return null;
        return new RoadmapSummaryResponse(
                r.getId(),
                r.getTitle(),
                r.getDescription(),
                r.getRoadmapType(),
                r.getTargetOpportunity() != null ? r.getTargetOpportunity().getId() : null,
                r.getTargetOpportunity() != null ? r.getTargetOpportunity().getTitle() : null,
                r.getTargetGoal(),
                r.getStatus(),
                r.getStartDate(),
                r.getTargetCompletionDate(),
                r.getEstimatedDurationWeeks(),
                r.getAvailableHoursPerWeek(),
                progress != null ? progress : 0.0,
                r.getMilestones() != null ? r.getMilestones().size() : 0,
                completedMilestones,
                totalTasks,
                completedTasks,
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
