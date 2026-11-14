package com.nexus.roadmap.dto;

import com.nexus.roadmap.entity.LearningResource;
import com.nexus.roadmap.entity.ResourceType;
import com.nexus.roadmap.entity.VerificationStatus;
import java.util.UUID;

public record LearningResourceResponse(
        UUID id,
        String title,
        String description,
        String resourceUrl,
        ResourceType resourceType,
        String provider,
        String skillName,
        String difficultyLevel,
        String estimatedDuration,
        VerificationStatus verificationStatus
) {
    public static LearningResourceResponse from(LearningResource r) {
        if (r == null) return null;
        return new LearningResourceResponse(
                r.getId(),
                r.getTitle(),
                r.getDescription(),
                r.getResourceUrl(),
                r.getResourceType(),
                r.getProvider(),
                r.getSkillName(),
                r.getDifficultyLevel(),
                r.getEstimatedDuration(),
                r.getVerificationStatus()
        );
    }
}
