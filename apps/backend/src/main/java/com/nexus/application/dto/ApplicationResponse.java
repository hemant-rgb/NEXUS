package com.nexus.application.dto;

import com.nexus.application.entity.Application;
import com.nexus.application.entity.ApplicationPriority;
import com.nexus.application.entity.ApplicationStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ApplicationResponse(
        UUID id,
        UUID userId,
        UUID opportunityId,
        String opportunityTitle,
        UUID roadmapId,
        String roadmapTitle,
        String title,
        String organization,
        String applicationType,
        String applicationUrl,
        ApplicationStatus status,
        ApplicationPriority priority,
        LocalDate deadline,
        OffsetDateTime appliedAt,
        LocalDate expectedResponseDate,
        String userNotes,
        int activitiesCount,
        int documentsCount,
        int interviewsCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ApplicationResponse from(Application a) {
        if (a == null) return null;
        return new ApplicationResponse(
                a.getId(),
                a.getUser() != null ? a.getUser().getId() : null,
                a.getOpportunity() != null ? a.getOpportunity().getId() : null,
                a.getOpportunity() != null ? a.getOpportunity().getTitle() : null,
                a.getRoadmap() != null ? a.getRoadmap().getId() : null,
                a.getRoadmap() != null ? a.getRoadmap().getTitle() : null,
                a.getTitle(),
                a.getOrganization(),
                a.getApplicationType(),
                a.getApplicationUrl(),
                a.getStatus(),
                a.getPriority(),
                a.getDeadline(),
                a.getAppliedAt(),
                a.getExpectedResponseDate(),
                a.getUserNotes(),
                a.getActivities() != null ? a.getActivities().size() : 0,
                a.getDocuments() != null ? a.getDocuments().size() : 0,
                a.getInterviews() != null ? a.getInterviews().size() : 0,
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}
