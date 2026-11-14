package com.nexus.application.dto;

import com.nexus.application.entity.InterviewStageType;
import com.nexus.application.entity.InterviewStatus;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record InterviewRequest(
        @NotNull(message = "Stage type is required")
        InterviewStageType stageType,
        OffsetDateTime scheduledAt,
        Integer durationMinutes,
        String interviewerInfo,
        String meetingLink,
        String preparationNotes,
        InterviewStatus status,
        String outcomeNotes
) {}
