package com.nexus.application.dto;

import com.nexus.application.entity.Resume;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ResumeResponse(
        UUID id,
        UUID userId,
        String title,
        String versionName,
        UUID targetOpportunityId,
        String targetOpportunityTitle,
        String professionalSummary,
        String educationJson,
        String skillsJson,
        String experienceJson,
        String projectsJson,
        String contributionsJson,
        Boolean isPrimary,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ResumeResponse from(Resume r) {
        if (r == null) return null;
        return new ResumeResponse(
                r.getId(),
                r.getUser() != null ? r.getUser().getId() : null,
                r.getTitle(),
                r.getVersionName(),
                r.getTargetOpportunity() != null ? r.getTargetOpportunity().getId() : null,
                r.getTargetOpportunity() != null ? r.getTargetOpportunity().getTitle() : null,
                r.getProfessionalSummary(),
                r.getEducationJson(),
                r.getSkillsJson(),
                r.getExperienceJson(),
                r.getProjectsJson(),
                r.getContributionsJson(),
                r.getIsPrimary(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
