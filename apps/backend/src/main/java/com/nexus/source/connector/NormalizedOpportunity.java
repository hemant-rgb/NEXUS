package com.nexus.source.connector;

import java.time.LocalDate;
import java.util.List;

public record NormalizedOpportunity(
        String externalId,
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
        String rawMetadata
) {
}
