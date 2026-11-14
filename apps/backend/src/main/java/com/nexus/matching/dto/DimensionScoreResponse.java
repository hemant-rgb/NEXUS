package com.nexus.matching.dto;

public record DimensionScoreResponse(
        double skills,
        double eligibility,
        double interest,
        double experience,
        double deadline,
        double location
) {}
