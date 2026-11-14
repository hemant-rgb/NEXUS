package com.nexus.roadmap.ai;

import java.util.List;

public record AiRoadmapPlanResult(
        String title,
        String description,
        String targetGoal,
        Integer estimatedWeeks,
        List<AiMilestonePlan> milestones,
        int promptTokens,
        int completionTokens,
        int totalTokens,
        String modelName
) {}
