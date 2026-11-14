package com.nexus.roadmap.dto;

import com.nexus.roadmap.entity.Roadmap;
import com.nexus.roadmap.entity.RoadmapStatus;
import com.nexus.roadmap.entity.RoadmapType;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record RoadmapResponse(
        UUID id,
        UUID userId,
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
        String generationVersion,
        Double overallProgressPercentage,
        int totalMilestones,
        int completedMilestones,
        int totalTasks,
        int completedTasks,
        Double remainingEstimatedHours,
        List<MilestoneResponse> milestones,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static RoadmapResponse from(Roadmap r, Double progress, int completedMilestones,
                                       int totalTasks, int completedTasks, Double remainingHours) {
        if (r == null) return null;
        List<MilestoneResponse> milestoneResponses = r.getMilestones() != null
                ? r.getMilestones().stream().map(MilestoneResponse::from).toList()
                : Collections.emptyList();

        return new RoadmapResponse(
                r.getId(),
                r.getUser() != null ? r.getUser().getId() : null,
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
                r.getGenerationVersion(),
                progress != null ? progress : 0.0,
                r.getMilestones() != null ? r.getMilestones().size() : 0,
                completedMilestones,
                totalTasks,
                completedTasks,
                remainingHours != null ? remainingHours : 0.0,
                milestoneResponses,
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
