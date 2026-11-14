package com.nexus.application.dto;

import com.nexus.application.entity.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateActivityRequest(
        @NotNull(message = "Activity type is required")
        ActivityType activityType,
        String fromStatus,
        String toStatus,
        @NotBlank(message = "Description is required")
        String description
) {}
