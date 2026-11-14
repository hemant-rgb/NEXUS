package com.nexus.pulse.dto;

import com.nexus.pulse.entity.Notification;
import com.nexus.pulse.entity.NotificationCategory;
import com.nexus.pulse.entity.NotificationPriority;
import com.nexus.pulse.entity.NotificationStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String notificationType,
        NotificationCategory category,
        String title,
        String message,
        NotificationPriority priority,
        NotificationStatus status,
        String relatedEntityType,
        String relatedEntityId,
        String actionUrl,
        OffsetDateTime createdAt,
        OffsetDateTime readAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getNotificationType(),
                n.getCategory(),
                n.getTitle(),
                n.getMessage(),
                n.getPriority(),
                n.getStatus(),
                n.getRelatedEntityType(),
                n.getRelatedEntityId(),
                n.getActionUrl(),
                n.getCreatedAt(),
                n.getReadAt()
        );
    }
}
