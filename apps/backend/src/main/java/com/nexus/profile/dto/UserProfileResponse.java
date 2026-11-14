package com.nexus.profile.dto;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        UUID userId,
        String fullName,
        String email,
        String educationLevel,
        String degree,
        String fieldOfStudy,
        String institution,
        Integer graduationYear,
        String countryCode,
        String timezone,
        String experienceLevel,
        String bio,
        String githubUsername,
        Integer availabilityHours,
        boolean onboardingCompleted,
        int onboardingStep,
        Instant updatedAt
) {
}
