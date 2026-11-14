package com.nexus.github.dto;

import com.nexus.github.entity.GitHubConnection;
import java.time.OffsetDateTime;
import java.util.UUID;

public record GitHubConnectionResponse(
        UUID id,
        String githubUsername,
        String avatarUrl,
        String profileUrl,
        Integer publicReposCount,
        Integer followersCount,
        Integer followingCount,
        String status,
        OffsetDateTime connectedAt,
        OffsetDateTime lastSyncedAt
) {
    public static GitHubConnectionResponse from(GitHubConnection c) {
        if (c == null) return null;
        return new GitHubConnectionResponse(
                c.getId(),
                c.getGithubUsername(),
                c.getAvatarUrl(),
                c.getProfileUrl(),
                c.getPublicReposCount(),
                c.getFollowersCount(),
                c.getFollowingCount(),
                c.getStatus(),
                c.getConnectedAt(),
                c.getLastSyncedAt()
        );
    }
}
