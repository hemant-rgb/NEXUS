package com.nexus.github.entity;

import com.nexus.user.entity.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "saved_repositories", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_saved_repo", columnNames = {"user_id", "repository_id"})
})
public class SavedRepository {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repository_id", nullable = false)
    private GitHubRepository repository;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "saved_at", nullable = false, updatable = false)
    private OffsetDateTime savedAt;

    public SavedRepository() {
        this.savedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public SavedRepository(User user, GitHubRepository repository) {
        this(user, repository, null);
    }

    public SavedRepository(User user, GitHubRepository repository, String notes) {
        this();
        this.user = user;
        this.repository = repository;
        this.notes = notes;
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

    public GitHubRepository getRepository() {
        return repository;
    }

    public void setRepository(GitHubRepository repository) {
        this.repository = repository;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public OffsetDateTime getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(OffsetDateTime savedAt) {
        this.savedAt = savedAt;
    }
}
