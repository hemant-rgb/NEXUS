package com.nexus.profile.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserPreferencesResponse(
        UUID id,
        UUID userId,
        List<String> preferredCategories,
        String remotePreference,
        List<String> preferredCountries,
        Integer weeklyHours,
        List<String> interests,
        Instant updatedAt
) {
}
