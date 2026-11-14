package com.nexus.github.controller;

import com.nexus.common.dto.ApiResponse;
import com.nexus.common.security.UserPrincipal;
import com.nexus.github.dto.*;
import com.nexus.github.entity.ContributionStatus;
import com.nexus.github.service.GitHubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "NEXUS GitHub Intelligence & Contributions", description = "GitHub profile sync, repository exploration, issue suitability matching, and contribution management")
public class GitHubController {

    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/github/connection")
    @Operation(summary = "Get current developer's GitHub connection status")
    public ResponseEntity<ApiResponse<GitHubConnectionResponse>> getConnection(
            @AuthenticationPrincipal UserPrincipal principal) {
        return gitHubService.getConnection(principal.getId())
                .map(conn -> ResponseEntity.ok(ApiResponse.success("GitHub connection found", conn)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.success("No GitHub connection active", null)));
    }

    @PostMapping("/github/connect")
    @Operation(summary = "Connect or update GitHub personal access token with AES-256 encryption")
    public ResponseEntity<ApiResponse<GitHubConnectionResponse>> connect(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ConnectGitHubRequest request) {
        GitHubConnectionResponse response = gitHubService.connect(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("GitHub account connected successfully", response));
    }

    @DeleteMapping("/github/disconnect")
    @Operation(summary = "Revoke and disconnect GitHub integration")
    public ResponseEntity<ApiResponse<Void>> disconnect(
            @AuthenticationPrincipal UserPrincipal principal) {
        gitHubService.disconnect(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("GitHub account disconnected successfully", null));
    }

    @GetMapping("/github/profile")
    @Operation(summary = "Get GitHub profile intelligence metrics")
    public ResponseEntity<ApiResponse<GitHubConnectionResponse>> getProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return gitHubService.getConnection(principal.getId())
                .map(conn -> ResponseEntity.ok(ApiResponse.success("GitHub profile retrieved", conn)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No active GitHub connection")));
    }

    @GetMapping("/github/repositories")
    @Operation(summary = "Search open-source repositories by keyword and programming language")
    public ResponseEntity<ApiResponse<List<GitHubRepoResponse>>> searchRepositories(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String language) {
        List<GitHubRepoResponse> repos = gitHubService.searchRepositories(principal != null ? principal.getId() : null, query, language);
        return ResponseEntity.ok(ApiResponse.success("Repositories retrieved successfully", repos));
    }

    @GetMapping("/github/repositories/saved")
    @Operation(summary = "Get developer's bookmarked repositories")
    public ResponseEntity<ApiResponse<List<GitHubRepoResponse>>> getSavedRepositories(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<GitHubRepoResponse> repos = gitHubService.getSavedRepositories(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Saved repositories retrieved successfully", repos));
    }

    @GetMapping("/github/repositories/{repositoryId}")
    @Operation(summary = "Get detailed repository metadata and contributing guidelines")
    public ResponseEntity<ApiResponse<GitHubRepoResponse>> getRepository(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID repositoryId) {
        GitHubRepoResponse repo = gitHubService.getRepository(principal != null ? principal.getId() : null, repositoryId);
        return ResponseEntity.ok(ApiResponse.success("Repository retrieved successfully", repo));
    }

    @PostMapping("/github/repositories/{repositoryId}/save")
    @Operation(summary = "Bookmark repository to developer's saved list")
    public ResponseEntity<ApiResponse<Void>> saveRepository(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID repositoryId) {
        gitHubService.saveRepository(principal.getId(), repositoryId);
        return ResponseEntity.ok(ApiResponse.success("Repository saved successfully", null));
    }

    @DeleteMapping("/github/repositories/{repositoryId}/save")
    @Operation(summary = "Remove repository from bookmarks")
    public ResponseEntity<ApiResponse<Void>> unsaveRepository(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID repositoryId) {
        gitHubService.unsaveRepository(principal.getId(), repositoryId);
        return ResponseEntity.ok(ApiResponse.success("Repository removed from saved", null));
    }

    @PostMapping("/github/repositories/{repositoryId}/analysis")
    @Operation(summary = "Generate AI architectural overview and orientation roadmap for repository")
    public ResponseEntity<ApiResponse<GitHubAiAnalysisResponse>> analyzeRepository(
            @PathVariable UUID repositoryId) {
        GitHubAiAnalysisResponse response = gitHubService.analyzeRepository(repositoryId);
        return ResponseEntity.ok(ApiResponse.success("Repository architectural analysis generated", response));
    }

    @GetMapping("/github/issues")
    @Operation(summary = "Search open-source issues with suitability scores and filters")
    public ResponseEntity<ApiResponse<List<GitHubIssueResponse>>> searchIssues(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Boolean goodFirstOnly,
            @RequestParam(required = false) Boolean helpWantedOnly,
            @RequestParam(required = false) String state) {
        List<GitHubIssueResponse> issues = gitHubService.searchIssues(
                principal != null ? principal.getId() : null, query, language, goodFirstOnly, helpWantedOnly, state);
        return ResponseEntity.ok(ApiResponse.success("Issues retrieved successfully", issues));
    }

    @GetMapping("/github/issues/saved")
    @Operation(summary = "Get developer's saved issues")
    public ResponseEntity<ApiResponse<List<GitHubIssueResponse>>> getSavedIssues(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<GitHubIssueResponse> issues = gitHubService.getSavedIssues(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Saved issues retrieved successfully", issues));
    }

    @GetMapping("/github/issues/{issueId}")
    @Operation(summary = "Get issue details with personal suitability score")
    public ResponseEntity<ApiResponse<GitHubIssueResponse>> getIssue(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID issueId) {
        GitHubIssueResponse issue = gitHubService.getIssue(principal != null ? principal.getId() : null, issueId);
        return ResponseEntity.ok(ApiResponse.success("Issue retrieved successfully", issue));
    }

    @PostMapping("/github/issues/{issueId}/save")
    @Operation(summary = "Bookmark issue to saved list")
    public ResponseEntity<ApiResponse<Void>> saveIssue(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID issueId) {
        gitHubService.saveIssue(principal.getId(), issueId);
        return ResponseEntity.ok(ApiResponse.success("Issue saved successfully", null));
    }

    @DeleteMapping("/github/issues/{issueId}/save")
    @Operation(summary = "Remove issue from saved list")
    public ResponseEntity<ApiResponse<Void>> unsaveIssue(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID issueId) {
        gitHubService.unsaveIssue(principal.getId(), issueId);
        return ResponseEntity.ok(ApiResponse.success("Issue removed from saved", null));
    }

    @GetMapping("/github/issues/{issueId}/suitability")
    @Operation(summary = "Calculate deterministic suitability analysis for issue based on user skills")
    public ResponseEntity<ApiResponse<IssueSuitabilityResponse>> getIssueSuitability(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID issueId) {
        IssueSuitabilityResponse response = gitHubService.getIssueSuitability(principal.getId(), issueId);
        return ResponseEntity.ok(ApiResponse.success("Issue suitability analyzed successfully", response));
    }

    @PostMapping("/github/issues/{issueId}/analysis")
    @Operation(summary = "Generate AI implementation blueprint and testing approach for issue")
    public ResponseEntity<ApiResponse<GitHubAiAnalysisResponse>> analyzeIssue(
            @PathVariable UUID issueId) {
        GitHubAiAnalysisResponse response = gitHubService.analyzeIssue(issueId);
        return ResponseEntity.ok(ApiResponse.success("Issue implementation blueprint generated", response));
    }

    // Contribution Workspaces
    @GetMapping("/users/me/contributions")
    @Operation(summary = "List developer's open-source contribution workspaces")
    public ResponseEntity<ApiResponse<List<ContributionWorkspaceResponse>>> getContributionWorkspaces(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) ContributionStatus status) {
        List<ContributionWorkspaceResponse> response = gitHubService.getContributionWorkspaces(principal.getId(), status);
        return ResponseEntity.ok(ApiResponse.success("Contribution workspaces retrieved", response));
    }

    @PostMapping("/users/me/contributions")
    @Operation(summary = "Create an active contribution workspace for an issue or repository")
    public ResponseEntity<ApiResponse<ContributionWorkspaceResponse>> createContributionWorkspace(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ContributionWorkspaceRequest request) {
        ContributionWorkspaceResponse response = gitHubService.createContributionWorkspace(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Contribution workspace initialized", response));
    }

    @GetMapping("/contributions/{id}")
    @Operation(summary = "Get single contribution workspace details")
    public ResponseEntity<ApiResponse<ContributionWorkspaceResponse>> getContributionWorkspace(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        ContributionWorkspaceResponse response = gitHubService.getContributionWorkspace(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Contribution workspace retrieved", response));
    }

    @PatchMapping("/contributions/{id}")
    @Operation(summary = "Update contribution status, research notes, solution plan, or PR link")
    public ResponseEntity<ApiResponse<ContributionWorkspaceResponse>> updateContributionWorkspace(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody ContributionWorkspaceRequest request) {
        ContributionWorkspaceResponse response = gitHubService.updateContributionWorkspace(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Contribution workspace updated", response));
    }

    @DeleteMapping("/contributions/{id}")
    @Operation(summary = "Delete contribution workspace")
    public ResponseEntity<ApiResponse<Void>> deleteContributionWorkspace(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        gitHubService.deleteContributionWorkspace(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Contribution workspace deleted", null));
    }
}
