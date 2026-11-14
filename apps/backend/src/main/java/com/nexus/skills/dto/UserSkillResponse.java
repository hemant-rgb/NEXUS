package com.nexus.skills.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UserSkillResponse(
        UUID id,
        UUID skillId,
        String skillName,
        String category,
        String proficiency,
        BigDecimal yearsExperience,
        Instant createdAt
) {
}
