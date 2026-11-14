package com.nexus.source.connector;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public record SyncResult(
        String sourceKey,
        int totalFetched,
        List<NormalizedOpportunity> opportunities,
        boolean success,
        String errorMessage,
        Instant syncedAt
) {
    public static SyncResult success(String sourceKey, List<NormalizedOpportunity> opportunities) {
        return new SyncResult(sourceKey, opportunities.size(), opportunities, true, null, Instant.now());
    }

    public static SyncResult failure(String sourceKey, String errorMessage) {
        return new SyncResult(sourceKey, 0, Collections.emptyList(), false, errorMessage, Instant.now());
    }
}
