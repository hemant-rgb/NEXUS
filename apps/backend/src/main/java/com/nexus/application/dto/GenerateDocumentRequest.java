package com.nexus.application.dto;

import com.nexus.application.entity.DocumentType;
import jakarta.validation.constraints.NotNull;

public record GenerateDocumentRequest(
        @NotNull(message = "Document type is required")
        DocumentType documentType,
        String title,
        String userInstructions,
        String customPrompt
) {}
