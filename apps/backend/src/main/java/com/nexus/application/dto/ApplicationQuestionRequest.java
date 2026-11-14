package com.nexus.application.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ApplicationQuestionRequest(
        @NotBlank(message = "Question is required")
        String question,
        String context
) {}
