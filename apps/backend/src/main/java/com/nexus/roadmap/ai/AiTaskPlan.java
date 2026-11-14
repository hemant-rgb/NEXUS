package com.nexus.roadmap.ai;

import com.nexus.roadmap.entity.TaskPriority;
import com.nexus.roadmap.entity.TaskType;

public record AiTaskPlan(
        String title,
        String description,
        String completionCriteria,
        TaskType taskType,
        TaskPriority priority,
        Double estimatedHours,
        Integer sequenceOrder,
        String skillName,
        String recommendedResourceTitle,
        String recommendedResourceUrl
) {}
