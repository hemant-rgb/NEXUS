package com.nexus.matching.dto;

public record SkillItemResponse(
        String skillName,
        String status, // MATCHED, PARTIALLY_MATCHED, MISSING, UNKNOWN
        String userProficiency,
        String priority, // HIGH, MEDIUM, LOW
        boolean isRequired
) {}
