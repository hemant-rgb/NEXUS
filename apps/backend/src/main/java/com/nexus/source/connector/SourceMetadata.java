package com.nexus.source.connector;

public record SourceMetadata(
        String sourceKey,
        String name,
        String sourceType,
        String description,
        String baseUrl
) {
}
