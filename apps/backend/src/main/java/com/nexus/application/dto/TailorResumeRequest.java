package com.nexus.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TailorResumeRequest(
        @NotNull(message = "Target opportunity ID is required")
        UUID targetOpportunityId,
        String instructions
) {}
