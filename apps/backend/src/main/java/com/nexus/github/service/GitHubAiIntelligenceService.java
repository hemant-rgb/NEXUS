package com.nexus.github.service;

import com.nexus.github.dto.GitHubAiAnalysisResponse;
import com.nexus.github.entity.GitHubIssue;
import com.nexus.github.entity.GitHubRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GitHubAiIntelligenceService {

    public GitHubAiAnalysisResponse analyzeRepository(GitHubRepository repo) {
        String repoName = repo != null ? repo.getFullName() : "Open Source Project";
        String lang = repo != null && repo.getPrimaryLanguage() != null ? repo.getPrimaryLanguage() : "Multi-language";
        String desc = repo != null && repo.getDescription() != null ? repo.getDescription() : "Open source software project";

        List<String> keyComponents = new ArrayList<>();
        keyComponents.add("Primary Language: " + lang);
        if (repo != null && repo.getTopics() != null && !repo.getTopics().isBlank()) {
            keyComponents.add("Core Topics & Ecosystem: " + repo.getTopics());
        }
        keyComponents.add("License: " + (repo != null && repo.getLicenseSpdx() != null ? repo.getLicenseSpdx() : "Open Source"));
        keyComponents.add("Default Branch: " + (repo != null ? repo.getDefaultBranch() : "main"));

        List<String> suggestedSteps = new ArrayList<>();
        suggestedSteps.add("Fork the repository and clone your fork locally: `git clone <fork-url>`");
        suggestedSteps.add("Inspect `CONTRIBUTING.md` and project code style conventions before making changes.");
        suggestedSteps.add("Set up local development toolchain for " + lang + " and verify the build passes with existing test suites.");
        suggestedSteps.add("Pick an issue tagged `good first issue` or `help wanted` to familiarize yourself with maintainer workflow.");

        String testingGuidance = "Run the full test suite locally before submitting PRs. Verify that all linter checks, static analyzers, and unit tests pass with zero warnings.";

        List<String> warnings = new ArrayList<>();
        warnings.add("Always create a dedicated feature branch for your changes (`git checkout -b feature/issue-xyz`).");
        warnings.add("Reference the issue number in your commit message and PR description (`Fixes #123`).");
        if (Boolean.TRUE.equals(repo != null && repo.getHasContributingGuide())) {
            warnings.add("Review the repository's CONTRIBUTING guide thoroughly: " + (repo.getContributingUrl() != null ? repo.getContributingUrl() : "in root directory"));
        }

        return new GitHubAiAnalysisResponse(
                "REPOSITORY",
                repoName,
                desc,
                "Architectural overview for " + repoName + " built in " + lang + ". Designed for modular maintainability with automated CI/CD checks.",
                keyComponents,
                suggestedSteps,
                testingGuidance,
                warnings
        );
    }

    public GitHubAiAnalysisResponse analyzeIssue(GitHubIssue issue) {
        String title = issue != null ? issue.getTitle() : "Issue";
        String repoName = issue != null && issue.getRepository() != null ? issue.getRepository().getFullName() : "Repository";
        String lang = issue != null && issue.getRepository() != null && issue.getRepository().getPrimaryLanguage() != null ? issue.getRepository().getPrimaryLanguage() : "General";

        List<String> prerequisites = new ArrayList<>();
        prerequisites.add("Working local development environment for " + lang);
        prerequisites.add("Understanding of the repository codebase orientation and component architecture");
        if (Boolean.TRUE.equals(issue != null && issue.getIsGoodFirstIssue())) {
            prerequisites.add("Beginner-friendly issue: no deep subsystem knowledge required.");
        }

        List<String> implementationSteps = new ArrayList<>();
        implementationSteps.add("Locate the relevant source files and tests corresponding to the reported behavior.");
        implementationSteps.add("Reproduce the issue locally with a failing unit test or reproduction script.");
        implementationSteps.add("Implement the minimal, focused change addressing the core problem without side effects.");
        implementationSteps.add("Update documentation or code comments if public APIs or behaviors changed.");

        String testingGuidance = "Add focused regression tests reproducing the defect. Ensure all existing tests in the target package continue to pass.";

        List<String> tips = new ArrayList<>();
        tips.add("Keep the Pull Request focused strictly on this single issue.");
        tips.add("Include before/after reproduction evidence or test output in the PR body.");

        return new GitHubAiAnalysisResponse(
                "ISSUE",
                "#" + (issue != null ? issue.getIssueNumber() : "") + " " + title,
                "Implementation blueprint for issue #" + (issue != null ? issue.getIssueNumber() : "") + " in " + repoName,
                "Issue targets core logic. Proposed approach is to write a failing test case first, make the surgical fix, and verify zero regression.",
                prerequisites,
                implementationSteps,
                testingGuidance,
                tips
        );
    }
}
