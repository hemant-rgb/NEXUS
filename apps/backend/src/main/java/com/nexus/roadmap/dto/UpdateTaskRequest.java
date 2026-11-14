package com.nexus.roadmap.dto;

import com.nexus.roadmap.entity.TaskPriority;
import com.nexus.roadmap.entity.TaskStatus;
import com.nexus.roadmap.entity.TaskType;
import java.time.LocalDate;

public record UpdateTaskRequest(
        String title,
        String description,
        String completionCriteria,
        TaskType taskType,
        TaskPriority priority,
        Double estimatedHours,
        Integer sequenceOrder,
        LocalDate scheduledDate,
        TaskStatus status,
        String userNotes
) {}
