package com.nexus.github.dto;

import java.util.List;
import java.util.UUID;

public record IssueSuitabilityResponse(
        UUID issueId,
        String repositoryFullName,
        double suitabilityScore,
        List<String> matchingSkills,
        List<String> missingSkills,
        String experienceLevelSuitability,
        String difficultyAssessment,
        List<String> reasoning
) {}
