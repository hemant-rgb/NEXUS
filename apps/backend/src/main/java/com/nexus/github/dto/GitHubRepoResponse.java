package com.nexus.github.dto;

import com.nexus.github.entity.GitHubRepository;
import java.time.OffsetDateTime;
import java.util.UUID;

public record GitHubRepoResponse(
        UUID id,
        Long githubRepoId,
        String ownerLogin,
        String name,
        String fullName,
        String description,
        String htmlUrl,
        String primaryLanguage,
        String topics,
        Integer starsCount,
        Integer forksCount,
        Integer openIssuesCount,
        String defaultBranch,
        String licenseSpdx,
        Boolean hasContributingGuide,
        String contributingUrl,
        OffsetDateTime lastPushedAt,
        Boolean isSaved
) {
    public static GitHubRepoResponse from(GitHubRepository r, boolean isSaved) {
        if (r == null) return null;
        return new GitHubRepoResponse(
                r.getId(),
                r.getGithubRepoId(),
                r.getOwnerLogin(),
                r.getName(),
                r.getFullName(),
                r.getDescription(),
                r.getHtmlUrl(),
                r.getPrimaryLanguage(),
                r.getTopics(),
                r.getStarsCount(),
                r.getForksCount(),
                r.getOpenIssuesCount(),
                r.getDefaultBranch(),
                r.getLicenseSpdx(),
                r.getHasContributingGuide(),
                r.getContributingUrl(),
                r.getLastPushedAt(),
                isSaved
        );
    }
}
