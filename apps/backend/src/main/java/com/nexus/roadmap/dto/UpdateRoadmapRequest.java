package com.nexus.roadmap.dto;

import com.nexus.roadmap.entity.RoadmapStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

public record UpdateRoadmapRequest(
        String title,
        String description,
        RoadmapStatus status,
        @Min(value = 1, message = "Available hours must be at least 1")
        @Max(value = 80, message = "Available hours cannot exceed 80")
        Integer availableHoursPerWeek,
        LocalDate targetCompletionDate
) {}
