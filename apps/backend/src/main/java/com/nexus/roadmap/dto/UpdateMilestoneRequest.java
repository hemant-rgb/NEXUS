package com.nexus.roadmap.dto;

import com.nexus.roadmap.entity.MilestoneStatus;
import java.time.LocalDate;

public record UpdateMilestoneRequest(
        String title,
        String description,
        MilestoneStatus status,
        Integer sequenceOrder,
        LocalDate targetDate
) {}
