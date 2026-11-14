package com.nexus.source.dto;

import java.time.Instant;
import java.util.UUID;

public record SourceResponse(
        UUID id,
        String name,
        String sourceKey,
        String sourceType,
        String baseUrl,
        boolean enabled,
        Instant lastSuccessfulSyncAt,
        String lastSyncStatus,
        int totalRecordsSynced
) {
}
