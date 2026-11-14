package com.nexus.application.dto;

import com.nexus.application.entity.ActivityType;
import com.nexus.application.entity.ApplicationActivity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ActivityResponse(
        UUID id,
        UUID applicationId,
        ActivityType activityType,
        String fromStatus,
        String toStatus,
        String description,
        OffsetDateTime createdAt
) {
    public static ActivityResponse from(ApplicationActivity a) {
        if (a == null) return null;
        return new ActivityResponse(
                a.getId(),
                a.getApplication() != null ? a.getApplication().getId() : null,
                a.getActivityType(),
                a.getFromStatus(),
                a.getToStatus(),
                a.getDescription(),
                a.getCreatedAt()
        );
    }
}
