package com.nexus.skills.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateUserSkillRequest(
        UUID skillId,

        String skillName,

        @NotBlank(message = "Proficiency is required")
        @Pattern(regexp = "^(Beginner|Intermediate|Advanced)$", message = "Proficiency must be Beginner, Intermediate, or Advanced")
        String proficiency,

        @DecimalMin(value = "0.0", message = "Years of experience cannot be negative")
        @DecimalMax(value = "50.0", message = "Years of experience must be realistic")
        BigDecimal yearsExperience
) {
}
