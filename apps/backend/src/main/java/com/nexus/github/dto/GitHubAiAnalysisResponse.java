package com.nexus.github.dto;

import java.util.List;

public record GitHubAiAnalysisResponse(
        String targetType,       // "REPOSITORY" or "ISSUE"
        String targetName,
        String summary,
        String technicalOverview,
        List<String> keyComponentsOrPrerequisites,
        List<String> suggestedSteps,
        String testingGuidance,
        List<String> warningsOrTips
) {}
