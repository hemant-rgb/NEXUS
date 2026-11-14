package com.nexus.intelligence.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OpportunityAnalysisResponse(
        UUID id,
        UUID opportunityId,
        String opportunityTitle,
        String organization,
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
        String recommendation, // STRONG_FIT, POTENTIAL_FIT, WEAK_FIT, NEEDS_INFORMATION
        String sourceReferences,
        String modelName,
        String promptVersion,
        Instant generatedAt
) {}
