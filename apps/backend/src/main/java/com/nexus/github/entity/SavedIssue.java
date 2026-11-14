package com.nexus.github.entity;

import com.nexus.user.entity.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "saved_issues", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_saved_issue", columnNames = {"user_id", "issue_id"})
})
public class SavedIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issue_id", nullable = false)
    private GitHubIssue issue;

    @Column(name = "saved_at", nullable = false, updatable = false)
    private OffsetDateTime savedAt;

    public SavedIssue() {
        this.savedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public SavedIssue(User user, GitHubIssue issue) {
        this();
        this.user = user;
        this.issue = issue;
    }

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

    public GitHubIssue getIssue() {
        return issue;
    }

    public void setIssue(GitHubIssue issue) {
        this.issue = issue;
    }

    public OffsetDateTime getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(OffsetDateTime savedAt) {
        this.savedAt = savedAt;
    }
}
