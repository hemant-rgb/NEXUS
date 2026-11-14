package com.nexus.opportunity.dto;

import jakarta.validation.constraints.Size;

public record UpdateSavedOpportunityRequest(
        @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
        String notes
) {
}
