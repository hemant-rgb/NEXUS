package com.nexus.github.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "github_repositories")
public class GitHubRepository {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "github_repo_id", nullable = false, unique = true)
    private Long githubRepoId;

    @Column(name = "owner_login", nullable = false, length = 100)
    private String ownerLogin;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "html_url", nullable = false, columnDefinition = "TEXT")
    private String htmlUrl;

    @Column(name = "primary_language", length = 100)
    private String primaryLanguage;

    @Column(columnDefinition = "TEXT")
    private String topics; // JSON string array

    @Column(name = "stars_count")
    private Integer starsCount = 0;

    @Column(name = "forks_count")
    private Integer forksCount = 0;

    @Column(name = "open_issues_count")
    private Integer openIssuesCount = 0;

    @Column(name = "default_branch", length = 100)
    private String defaultBranch = "main";

    @Column(name = "license_spdx", length = 100)
    private String licenseSpdx;

    @Column(name = "has_contributing_guide")
    private Boolean hasContributingGuide = false;

    @Column(name = "contributing_url", columnDefinition = "TEXT")
    private String contributingUrl;

    @Column(name = "last_pushed_at")
    private OffsetDateTime lastPushedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public GitHubRepository() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public GitHubRepository(Long githubRepoId, String ownerLogin, String name, String fullName,
                            String description, String htmlUrl, String primaryLanguage,
                            String topics, Integer starsCount, Integer forksCount,
                            Integer openIssuesCount, String defaultBranch, String licenseSpdx) {
        this(githubRepoId, ownerLogin, name, fullName, description, htmlUrl, primaryLanguage, topics,
                starsCount, forksCount, openIssuesCount, defaultBranch, licenseSpdx, false, null, null);
    }

    public GitHubRepository(Long githubRepoId, String ownerLogin, String name, String fullName,
                            String description, String htmlUrl, String primaryLanguage,
                            String topics, Integer starsCount, Integer forksCount,
                            Integer openIssuesCount, String defaultBranch, String licenseSpdx,
                            Boolean hasContributingGuide, String contributingUrl, OffsetDateTime lastPushedAt) {
        this();
        this.githubRepoId = githubRepoId;
        this.ownerLogin = ownerLogin;
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        this.htmlUrl = htmlUrl;
        this.primaryLanguage = primaryLanguage;
        this.topics = topics;
        this.starsCount = starsCount != null ? starsCount : 0;
        this.forksCount = forksCount != null ? forksCount : 0;
        this.openIssuesCount = openIssuesCount != null ? openIssuesCount : 0;
        this.defaultBranch = defaultBranch != null ? defaultBranch : "main";
        this.licenseSpdx = licenseSpdx;
        this.hasContributingGuide = hasContributingGuide != null ? hasContributingGuide : false;
        this.contributingUrl = contributingUrl;
        this.lastPushedAt = lastPushedAt;
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

    public Long getGithubRepoId() {
        return githubRepoId;
    }

    public void setGithubRepoId(Long githubRepoId) {
        this.githubRepoId = githubRepoId;
    }

    public String getOwnerLogin() {
        return ownerLogin;
    }

    public void setOwnerLogin(String ownerLogin) {
        this.ownerLogin = ownerLogin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getPrimaryLanguage() {
        return primaryLanguage;
    }

    public void setPrimaryLanguage(String primaryLanguage) {
        this.primaryLanguage = primaryLanguage;
    }

    public String getTopics() {
        return topics;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }

    public Integer getStarsCount() {
        return starsCount;
    }

    public void setStarsCount(Integer starsCount) {
        this.starsCount = starsCount;
    }

    public Integer getForksCount() {
        return forksCount;
    }

    public void setForksCount(Integer forksCount) {
        this.forksCount = forksCount;
    }

    public Integer getOpenIssuesCount() {
        return openIssuesCount;
    }

    public void setOpenIssuesCount(Integer openIssuesCount) {
        this.openIssuesCount = openIssuesCount;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    public String getLicenseSpdx() {
        return licenseSpdx;
    }

    public void setLicenseSpdx(String licenseSpdx) {
        this.licenseSpdx = licenseSpdx;
    }

    public Boolean getHasContributingGuide() {
        return hasContributingGuide;
    }

    public void setHasContributingGuide(Boolean hasContributingGuide) {
        this.hasContributingGuide = hasContributingGuide;
    }

    public String getContributingUrl() {
        return contributingUrl;
    }

    public void setContributingUrl(String contributingUrl) {
        this.contributingUrl = contributingUrl;
    }

    public OffsetDateTime getLastPushedAt() {
        return lastPushedAt;
    }

    public void setLastPushedAt(OffsetDateTime lastPushedAt) {
        this.lastPushedAt = lastPushedAt;
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
