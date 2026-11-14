package com.nexus.roadmap.dto;

import com.nexus.roadmap.entity.RoadmapType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateRoadmapRequest(
        @NotNull(message = "Roadmap type is required")
        RoadmapType roadmapType,

        UUID opportunityId,

        String targetGoal,

        String targetDomain,

        String currentSkillLevel,

        List<String> targetSkills,

        @Min(value = 1, message = "Available hours must be at least 1 hour per week")
        @Max(value = 80, message = "Available hours cannot exceed 80 hours per week")
        Integer availableHoursPerWeek,

        LocalDate targetCompletionDate,

        String customTitle
) {}
