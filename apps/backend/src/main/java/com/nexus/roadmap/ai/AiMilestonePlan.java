package com.nexus.roadmap.ai;

import java.util.List;

public record AiMilestonePlan(
        String title,
        String description,
        Integer sequenceOrder,
        Integer durationWeeks,
        List<AiTaskPlan> tasks
) {}
