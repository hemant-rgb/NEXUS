package com.nexus.profile.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @Size(max = 100, message = "Full name cannot exceed 100 characters")
        String fullName,

        @Size(max = 100, message = "Education level cannot exceed 100 characters")
        String educationLevel,

        @Size(max = 150, message = "Degree cannot exceed 150 characters")
        String degree,

        @Size(max = 150, message = "Field of study cannot exceed 150 characters")
        String fieldOfStudy,

        @Size(max = 200, message = "Institution cannot exceed 200 characters")
        String institution,

        @Min(value = 1950, message = "Graduation year must be valid")
        @Max(value = 2050, message = "Graduation year must be valid")
        Integer graduationYear,

        @Size(max = 10, message = "Country code cannot exceed 10 characters")
        String countryCode,

        @Size(max = 100, message = "Timezone cannot exceed 100 characters")
        String timezone,

        @Size(max = 50, message = "Experience level cannot exceed 50 characters")
        String experienceLevel,

        @Size(max = 2000, message = "Bio cannot exceed 2000 characters")
        String bio,

        @Size(max = 100, message = "GitHub username cannot exceed 100 characters")
        String githubUsername,

        @Min(value = 0, message = "Availability hours cannot be negative")
        @Max(value = 168, message = "Availability hours cannot exceed 168")
        Integer availabilityHours,

        Boolean onboardingCompleted,

        @Min(value = 1, message = "Onboarding step must be between 1 and 6")
        @Max(value = 6, message = "Onboarding step must be between 1 and 6")
        Integer onboardingStep
) {
}
