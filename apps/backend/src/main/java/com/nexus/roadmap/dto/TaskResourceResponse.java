package com.nexus.roadmap.dto;

import com.nexus.roadmap.entity.TaskResource;
import java.util.UUID;

public record TaskResourceResponse(
        UUID id,
        UUID taskId,
        UUID resourceId,
        String title,
        String url,
        String provider,
        String resourceType,
        String verificationStatus
) {
    public static TaskResourceResponse from(TaskResource tr) {
        if (tr == null) return null;
        if (tr.getResource() != null) {
            return new TaskResourceResponse(
                    tr.getId(),
                    tr.getTask().getId(),
                    tr.getResource().getId(),
                    tr.getResource().getTitle(),
                    tr.getResource().getResourceUrl(),
                    tr.getResource().getProvider(),
                    tr.getResource().getResourceType().name(),
                    tr.getResource().getVerificationStatus().name()
            );
        } else {
            return new TaskResourceResponse(
                    tr.getId(),
                    tr.getTask().getId(),
                    null,
                    tr.getCustomTitle() != null ? tr.getCustomTitle() : "External Link",
                    tr.getCustomUrl(),
                    tr.getCustomProvider() != null ? tr.getCustomProvider() : "Custom",
                    "ARTICLE",
                    "COMMUNITY"
            );
        }
    }
}
