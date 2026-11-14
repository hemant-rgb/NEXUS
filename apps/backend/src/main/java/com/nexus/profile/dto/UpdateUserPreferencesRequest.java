package com.nexus.profile.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateUserPreferencesRequest(
        List<String> preferredCategories,

        @Size(max = 50, message = "Remote preference cannot exceed 50 characters")
        String remotePreference,

        List<String> preferredCountries,

        @Min(value = 0, message = "Weekly hours cannot be negative")
        @Max(value = 168, message = "Weekly hours cannot exceed 168")
        Integer weeklyHours,

        List<String> interests
) {
}
