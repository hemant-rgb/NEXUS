package com.nexus.application.dto;

import com.nexus.application.entity.ApplicationPriority;
import com.nexus.application.entity.ApplicationStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UpdateApplicationRequest(
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
        UUID roadmapId
) {}
