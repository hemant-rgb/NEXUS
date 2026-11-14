package com.nexus.matching.dto;

import com.nexus.opportunity.dto.OpportunityResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OpportunityMatchResponse(
        UUID id,
        UUID opportunityId,
        OpportunityResponse opportunity,
        double overallScore,
        DimensionScoreResponse dimensionScores,
        EligibilityAssessmentResponse eligibility,
        List<String> matchedSkills,
        List<String> missingRequiredSkills,
        List<String> missingPreferredSkills,
        List<String> positiveFactors,
        List<String> negativeFactors,
        String recommendationReason,
        String algorithmVersion,
        Instant calculatedAt
) {}
