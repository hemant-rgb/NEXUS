package com.nexus.pulse.dto;

import com.nexus.pulse.entity.NotificationCategory;
import com.nexus.pulse.entity.NotificationPriority;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ActivityItemResponse(
        UUID id,
        String type,
        NotificationCategory category,
        String title,
        String description,
        NotificationPriority priority,
        String actionUrl,
        OffsetDateTime timestamp
) {}
