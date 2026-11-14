package com.nexus.pulse.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GitHubIssueUpdatedEvent(
        String eventId,
        UUID userId,
        UUID issueId,
        String repoFullName,
        int issueNumber,
        String title,
        String state,
        OffsetDateTime timestamp
) implements NexusDomainEvent {
    public static GitHubIssueUpdatedEvent of(UUID userId, UUID issueId, String repoFullName, int issueNumber, String title, String state) {
        return new GitHubIssueUpdatedEvent(
                UUID.randomUUID().toString(),
                userId,
                issueId,
                repoFullName,
                issueNumber,
                title,
                state,
                OffsetDateTime.now()
        );
    }

    @Override
    public String getEventId() { return eventId; }

    @Override
    public String getEventType() { return "GITHUB_ISSUE_UPDATED"; }

    @Override
    public UUID getUserId() { return userId; }

    @Override
    public OffsetDateTime getTimestamp() { return timestamp; }
}
