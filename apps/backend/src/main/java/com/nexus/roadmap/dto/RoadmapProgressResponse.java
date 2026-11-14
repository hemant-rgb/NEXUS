package com.nexus.roadmap.dto;

import com.nexus.roadmap.entity.RoadmapStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RoadmapProgressResponse(
        UUID roadmapId,
        RoadmapStatus status,
        Double overallProgressPercentage,
        int totalMilestones,
        int completedMilestones,
        int totalTasks,
        int completedTasks,
        int inProgressTasks,
        int blockedTasks,
        Double totalEstimatedHours,
        Double remainingEstimatedHours,
        LocalDate projectedCompletionDate,
        List<TaskResponse> overdueTasks,
        List<TaskResponse> upcomingTasks
) {}
