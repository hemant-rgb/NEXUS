package com.nexus.intelligence.ai;

import java.util.List;

public record AiAnalysisResult(
        String summary,
        String organizationOverview,
        String expectations,
        String keyDates,
        String applicationProcess,
        String requirementsAnalysis,
        String personalizedAssessment,
        List<String> strengths,
        List<String> weaknesses,
        List<String> missingInformation,
        String eligibilityConcerns,
        String recommendation,
        String sourceReferences,
        String modelName,
        int promptTokens,
        int completionTokens,
        int totalTokens,
        long durationMs
) {}
