package com.nexus.source.connector;

import java.time.Instant;

public record SyncRequest(
        boolean incremental,
        Instant since,
        int batchSize
) {
    public static SyncRequest full() {
        return new SyncRequest(false, null, 100);
    }
}
