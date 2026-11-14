package com.nexus.matching.dto;

import java.util.List;
import java.util.UUID;

public record SkillGapReportResponse(
        UUID opportunityId,
        String opportunityTitle,
        List<SkillItemResponse> matchedSkills,
        List<SkillItemResponse> partiallyMatchedSkills,
        List<SkillItemResponse> missingRequiredSkills,
        List<SkillItemResponse> missingPreferredSkills,
        int totalSkillsEvaluated,
        double matchPercentage
) {}
