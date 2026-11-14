package com.nexus.agent.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record StartAgentRunRequest(
        @NotBlank(message = "Goal is required")
        String goal,
        Map<String, Object> parameters
) {}
