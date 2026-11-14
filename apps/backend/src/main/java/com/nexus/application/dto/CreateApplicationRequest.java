package com.nexus.application.dto;

import com.nexus.application.entity.ApplicationPriority;
import com.nexus.application.entity.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.UUID;

public record CreateApplicationRequest(
        UUID opportunityId,
        UUID roadmapId,
        @NotBlank(message = "Title is required")
        String title,
        @NotBlank(message = "Organization is required")
        String organization,
        String applicationType,
        String applicationUrl,
        ApplicationStatus status,
        ApplicationPriority priority,
        LocalDate deadline,
        LocalDate expectedResponseDate,
        String userNotes
) {}
