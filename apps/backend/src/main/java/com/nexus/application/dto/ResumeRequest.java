package com.nexus.application.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ResumeRequest(
        @NotBlank(message = "Resume title is required")
        String title,
        String versionName,
        UUID targetOpportunityId,
        String professionalSummary,
        String educationJson,
        String skillsJson,
        String experienceJson,
        String projectsJson,
        String contributionsJson,
        Boolean isPrimary
) {}
