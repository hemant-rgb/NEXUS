package com.nexus.application.dto;

import com.nexus.application.entity.ApplicationInterview;
import com.nexus.application.entity.InterviewStageType;
import com.nexus.application.entity.InterviewStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record InterviewResponse(
        UUID id,
        UUID applicationId,
        InterviewStageType stageType,
        OffsetDateTime scheduledAt,
        Integer durationMinutes,
        String interviewerInfo,
        String meetingLink,
        String preparationNotes,
        InterviewStatus status,
        String outcomeNotes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static InterviewResponse from(ApplicationInterview i) {
        if (i == null) return null;
        return new InterviewResponse(
                i.getId(),
                i.getApplication() != null ? i.getApplication().getId() : null,
                i.getStageType(),
                i.getScheduledAt(),
                i.getDurationMinutes(),
                i.getInterviewerInfo(),
                i.getMeetingLink(),
                i.getPreparationNotes(),
                i.getStatus(),
                i.getOutcomeNotes(),
                i.getCreatedAt(),
                i.getUpdatedAt()
        );
    }
}
