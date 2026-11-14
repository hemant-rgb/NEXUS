package com.nexus.github.dto;

import com.nexus.github.entity.GitHubIssue;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record GitHubIssueResponse(
        UUID id,
        Long githubIssueId,
        UUID repositoryId,
        String repositoryFullName,
        String primaryLanguage,
        Integer issueNumber,
        String title,
        String body,
        String htmlUrl,
        String state,
        List<String> labels,
        Integer commentsCount,
        Boolean isGoodFirstIssue,
        Boolean isHelpWanted,
        OffsetDateTime githubCreatedAt,
        OffsetDateTime githubUpdatedAt,
        Boolean isSaved,
        Double suitabilityScore
) {
    public static GitHubIssueResponse from(GitHubIssue issue, boolean isSaved, Double suitabilityScore) {
        if (issue == null) return null;
        List<String> labelList = parseLabels(issue.getLabels());
        return new GitHubIssueResponse(
                issue.getId(),
                issue.getGithubIssueId(),
                issue.getRepository() != null ? issue.getRepository().getId() : null,
                issue.getRepository() != null ? issue.getRepository().getFullName() : null,
                issue.getRepository() != null ? issue.getRepository().getPrimaryLanguage() : null,
                issue.getIssueNumber(),
                issue.getTitle(),
                issue.getBody(),
                issue.getHtmlUrl(),
                issue.getState(),
                labelList,
                issue.getCommentsCount(),
                issue.getIsGoodFirstIssue(),
                issue.getIsHelpWanted(),
                issue.getGithubCreatedAt(),
                issue.getGithubUpdatedAt(),
                isSaved,
                suitabilityScore
        );
    }

    private static List<String> parseLabels(String labelsStr) {
        if (labelsStr == null || labelsStr.isBlank()) return Collections.emptyList();
        String cleaned = labelsStr.trim();
        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return Arrays.stream(cleaned.split(","))
                .map(s -> s.replace("\"", "").replace("'", "").trim())
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
