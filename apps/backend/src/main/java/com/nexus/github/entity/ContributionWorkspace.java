package com.nexus.github.entity;

import com.nexus.roadmap.entity.RoadmapTask;
import com.nexus.user.entity.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "contribution_workspaces")
public class ContributionWorkspace {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repository_id", nullable = false)
    private GitHubRepository repository;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    private GitHubIssue issue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_task_id")
    private RoadmapTask roadmapTask;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ContributionStatus status = ContributionStatus.INTERESTED;

    @Column(name = "target_branch", length = 100)
    private String targetBranch = "main";

    @Column(name = "pull_request_url", columnDefinition = "TEXT")
    private String pullRequestUrl;

    @Column(name = "research_notes", columnDefinition = "TEXT")
    private String researchNotes;

    @Column(name = "solution_plan", columnDefinition = "TEXT")
    private String solutionPlan;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public ContributionWorkspace() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public ContributionWorkspace(User user, GitHubRepository repository, GitHubIssue issue,
                                 RoadmapTask roadmapTask, String title, ContributionStatus status) {
        this();
        this.user = user;
        this.repository = repository;
        this.issue = issue;
        this.roadmapTask = roadmapTask;
        this.title = title;
        this.status = status != null ? status : ContributionStatus.INTERESTED;
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public GitHubRepository getRepository() {
        return repository;
    }

    public void setRepository(GitHubRepository repository) {
        this.repository = repository;
    }

    public GitHubIssue getIssue() {
        return issue;
    }

    public void setIssue(GitHubIssue issue) {
        this.issue = issue;
    }

    public RoadmapTask getRoadmapTask() {
        return roadmapTask;
    }

    public void setRoadmapTask(RoadmapTask roadmapTask) {
        this.roadmapTask = roadmapTask;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ContributionStatus getStatus() {
        return status;
    }

    public void setStatus(ContributionStatus status) {
        this.status = status;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
    }

    public String getPullRequestUrl() {
        return pullRequestUrl;
    }

    public void setPullRequestUrl(String pullRequestUrl) {
        this.pullRequestUrl = pullRequestUrl;
    }

    public String getResearchNotes() {
        return researchNotes;
    }

    public void setResearchNotes(String researchNotes) {
        this.researchNotes = researchNotes;
    }

    public String getSolutionPlan() {
        return solutionPlan;
    }

    public void setSolutionPlan(String solutionPlan) {
        this.solutionPlan = solutionPlan;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
