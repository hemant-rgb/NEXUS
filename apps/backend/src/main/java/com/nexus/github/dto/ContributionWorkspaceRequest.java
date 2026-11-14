package com.nexus.github.dto;

import com.nexus.github.entity.ContributionStatus;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ContributionWorkspaceRequest(
        UUID repositoryId,
        UUID issueId,
        UUID roadmapTaskId,
        @NotBlank(message = "Workspace title is required")
        String title,
        ContributionStatus status,
        String targetBranch,
        String pullRequestUrl,
        String researchNotes,
        String solutionPlan
) {}
