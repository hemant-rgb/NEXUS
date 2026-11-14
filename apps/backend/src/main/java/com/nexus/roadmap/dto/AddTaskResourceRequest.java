package com.nexus.roadmap.dto;

import java.util.UUID;

public record AddTaskResourceRequest(
        UUID resourceId,
        String customTitle,
        String customUrl,
        String customProvider
) {}
