package com.nexus.matching.dto;

import java.util.List;

public record EligibilityAssessmentResponse(
        String status,
        String reason,
        List<String> missingProfileFields,
        List<String> criteriaChecked
) {}
