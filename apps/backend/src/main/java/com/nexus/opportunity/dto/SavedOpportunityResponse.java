package com.nexus.opportunity.dto;

import java.time.Instant;
import java.util.UUID;

public record SavedOpportunityResponse(
        UUID id,
        UUID opportunityId,
        OpportunityResponse opportunity,
        Instant savedAt,
        String notes
) {
}
