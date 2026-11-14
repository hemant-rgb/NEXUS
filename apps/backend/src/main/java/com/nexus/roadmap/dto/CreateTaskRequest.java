package com.nexus.roadmap.dto;

import com.nexus.roadmap.entity.TaskPriority;
import com.nexus.roadmap.entity.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTaskRequest(
        @NotNull(message = "Milestone ID is required")
        UUID milestoneId,

        @NotBlank(message = "Title is required")
        String title,

        String description,

        String completionCriteria,

        TaskType taskType,

        TaskPriority priority,

        Double estimatedHours,

        Integer sequenceOrder,

        LocalDate scheduledDate
) {}
