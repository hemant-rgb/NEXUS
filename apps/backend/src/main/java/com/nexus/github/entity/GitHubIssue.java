package com.nexus.github.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "github_issues")
public class GitHubIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "github_issue_id", nullable = false, unique = true)
    private Long githubIssueId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repository_id", nullable = false)
    private GitHubRepository repository;

    @Column(name = "issue_number", nullable = false)
    private Integer issueNumber;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "html_url", nullable = false, columnDefinition = "TEXT")
    private String htmlUrl;

    @Column(nullable = false, length = 50)
    private String state = "OPEN";

    @Column(columnDefinition = "TEXT")
    private String labels; // JSON array of label names

    @Column(name = "comments_count")
    private Integer commentsCount = 0;

    @Column(name = "is_good_first_issue")
    private Boolean isGoodFirstIssue = false;

    @Column(name = "is_help_wanted")
    private Boolean isHelpWanted = false;

    @Column(name = "github_created_at")
    private OffsetDateTime githubCreatedAt;

    @Column(name = "github_updated_at")
    private OffsetDateTime githubUpdatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public GitHubIssue() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public GitHubIssue(Long githubIssueId, GitHubRepository repository, Integer issueNumber,
                       String title, String body, String htmlUrl, String state,
                       String labels, Integer commentsCount, Boolean isGoodFirstIssue,
                       Boolean isHelpWanted, OffsetDateTime githubCreatedAt, OffsetDateTime githubUpdatedAt) {
        this();
        this.githubIssueId = githubIssueId;
        this.repository = repository;
        this.issueNumber = issueNumber;
        this.title = title;
        this.body = body;
        this.htmlUrl = htmlUrl;
        this.state = state != null ? state : "OPEN";
        this.labels = labels;
        this.commentsCount = commentsCount != null ? commentsCount : 0;
        this.isGoodFirstIssue = isGoodFirstIssue != null ? isGoodFirstIssue : false;
        this.isHelpWanted = isHelpWanted != null ? isHelpWanted : false;
        this.githubCreatedAt = githubCreatedAt;
        this.githubUpdatedAt = githubUpdatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getGithubIssueId() {
        return githubIssueId;
    }

    public void setGithubIssueId(Long githubIssueId) {
        this.githubIssueId = githubIssueId;
    }

    public GitHubRepository getRepository() {
        return repository;
    }

    public void setRepository(GitHubRepository repository) {
        this.repository = repository;
    }

    public Integer getIssueNumber() {
        return issueNumber;
    }

    public void setIssueNumber(Integer issueNumber) {
        this.issueNumber = issueNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }

    public Boolean getIsGoodFirstIssue() {
        return isGoodFirstIssue;
    }

    public void setIsGoodFirstIssue(Boolean isGoodFirstIssue) {
        this.isGoodFirstIssue = isGoodFirstIssue;
    }

    public Boolean getIsHelpWanted() {
        return isHelpWanted;
    }

    public void setIsHelpWanted(Boolean isHelpWanted) {
        this.isHelpWanted = isHelpWanted;
    }

    public OffsetDateTime getGithubCreatedAt() {
        return githubCreatedAt;
    }

    public void setGithubCreatedAt(OffsetDateTime githubCreatedAt) {
        this.githubCreatedAt = githubCreatedAt;
    }

    public OffsetDateTime getGithubUpdatedAt() {
        return githubUpdatedAt;
    }

    public void setGithubUpdatedAt(OffsetDateTime githubUpdatedAt) {
        this.githubUpdatedAt = githubUpdatedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
