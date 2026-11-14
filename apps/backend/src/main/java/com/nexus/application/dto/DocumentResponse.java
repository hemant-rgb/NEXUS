package com.nexus.application.dto;

import com.nexus.application.entity.ApplicationDocument;
import com.nexus.application.entity.DocumentType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        UUID applicationId,
        DocumentType documentType,
        String title,
        String content,
        String structuredSections,
        String version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static DocumentResponse from(ApplicationDocument d) {
        if (d == null) return null;
        return new DocumentResponse(
                d.getId(),
                d.getApplication() != null ? d.getApplication().getId() : null,
                d.getDocumentType(),
                d.getTitle(),
                d.getContent(),
                d.getStructuredSections(),
                d.getVersion(),
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }
}
