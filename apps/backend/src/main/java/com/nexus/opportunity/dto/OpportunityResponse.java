package com.nexus.opportunity.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record OpportunityResponse(
        UUID id,
        String title,
        String organization,
        String description,
        String category,
        String opportunityType,
        String location,
        String countryCode,
        boolean remoteAvailable,
        String eligibilityCriteria,
        List<String> requiredSkills,
        List<String> preferredSkills,
        String applicationUrl,
        String officialWebsiteUrl,
        LocalDate deadline,
        String deadlineType,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        Instant createdAt,
        Instant lastVerifiedAt,
        Boolean isSaved
) {
}
