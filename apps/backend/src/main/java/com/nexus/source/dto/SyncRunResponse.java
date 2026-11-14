package com.nexus.source.dto;

import java.time.Instant;
import java.util.UUID;

public record SyncRunResponse(
        UUID id,
        String sourceKey,
        String status,
        String syncType,
        Instant startedAt,
        Instant completedAt,
        int recordsFetched,
        int recordsCreated,
        int recordsUpdated,
        int recordsSkipped,
        int duplicatesDetected,
        String errorMessage,
        Long executionDurationMs
) {
}
