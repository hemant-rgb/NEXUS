package com.nexus.roadmap.ai;

public interface AiRoadmapPlanner {

    String getProviderName();

    boolean isAvailable();

    AiRoadmapPlanResult planRoadmap(AiRoadmapContext context);
}
