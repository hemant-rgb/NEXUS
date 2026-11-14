package com.nexus.github.dto;

import com.nexus.github.entity.ContributionStatus;
import com.nexus.github.entity.ContributionWorkspace;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ContributionWorkspaceResponse(
        UUID id,
        UUID repositoryId,
        String repositoryFullName,
        UUID issueId,
        Integer issueNumber,
        String issueTitle,
        String issueHtmlUrl,
        UUID roadmapTaskId,
        String title,
        ContributionStatus status,
        String targetBranch,
        String pullRequestUrl,
        String researchNotes,
        String solutionPlan,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ContributionWorkspaceResponse from(ContributionWorkspace w) {
        if (w == null) return null;
        return new ContributionWorkspaceResponse(
                w.getId(),
                w.getRepository() != null ? w.getRepository().getId() : null,
                w.getRepository() != null ? w.getRepository().getFullName() : null,
                w.getIssue() != null ? w.getIssue().getId() : null,
                w.getIssue() != null ? w.getIssue().getIssueNumber() : null,
                w.getIssue() != null ? w.getIssue().getTitle() : null,
                w.getIssue() != null ? w.getIssue().getHtmlUrl() : null,
                w.getRoadmapTask() != null ? w.getRoadmapTask().getId() : null,
                w.getTitle(),
                w.getStatus(),
                w.getTargetBranch(),
                w.getPullRequestUrl(),
                w.getResearchNotes(),
                w.getSolutionPlan(),
                w.getCreatedAt(),
                w.getUpdatedAt()
        );
    }
}
