package com.nexus.opportunity.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SaveOpportunityRequest(
        @NotNull(message = "Opportunity ID is required")
        UUID opportunityId,

        @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
        String notes
) {
}
