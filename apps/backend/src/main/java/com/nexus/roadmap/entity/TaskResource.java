package com.nexus.roadmap.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "task_resources")
public class TaskResource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private RoadmapTask task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private LearningResource resource;

    @Column(name = "custom_title", length = 255)
    private String customTitle;

    @Column(name = "custom_url", columnDefinition = "TEXT")
    private String customUrl;

    @Column(name = "custom_provider", length = 100)
    private String customProvider;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public TaskResource() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public TaskResource(RoadmapTask task, LearningResource resource) {
        this();
        this.task = task;
        this.resource = resource;
    }

    public TaskResource(RoadmapTask task, String customTitle, String customUrl, String customProvider) {
        this();
        this.task = task;
        this.customTitle = customTitle;
        this.customUrl = customUrl;
        this.customProvider = customProvider != null ? customProvider : "User Link";
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public RoadmapTask getTask() {
        return task;
    }

    public void setTask(RoadmapTask task) {
        this.task = task;
    }

    public LearningResource getResource() {
        return resource;
    }

    public void setResource(LearningResource resource) {
        this.resource = resource;
    }

    public String getCustomTitle() {
        return customTitle;
    }

    public void setCustomTitle(String customTitle) {
        this.customTitle = customTitle;
    }

    public String getCustomUrl() {
        return customUrl;
    }

    public void setCustomUrl(String customUrl) {
        this.customUrl = customUrl;
    }

    public String getCustomProvider() {
        return customProvider;
    }

    public void setCustomProvider(String customProvider) {
        this.customProvider = customProvider;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
