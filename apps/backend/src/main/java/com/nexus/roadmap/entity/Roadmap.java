package com.nexus.roadmap.entity;

import com.nexus.opportunity.entity.Opportunity;
import com.nexus.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "roadmaps")
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "roadmap_type", nullable = false, length = 50)
    private RoadmapType roadmapType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_opportunity_id")
    private Opportunity targetOpportunity;

    @Column(name = "target_goal", length = 255)
    private String targetGoal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RoadmapStatus status = RoadmapStatus.ACTIVE;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "target_completion_date")
    private LocalDate targetCompletionDate;

    @Column(name = "estimated_duration_weeks", nullable = false)
    private Integer estimatedDurationWeeks = 4;

    @Column(name = "available_hours_per_week", nullable = false)
    private Integer availableHoursPerWeek = 10;

    @Column(name = "generation_version", nullable = false, length = 50)
    private String generationVersion = "v1.0";

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    private List<RoadmapMilestone> milestones = new ArrayList<>();

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    private List<RoadmapTask> tasks = new ArrayList<>();

    public Roadmap() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Roadmap(User user, String title, String description, RoadmapType roadmapType,
                   Opportunity targetOpportunity, String targetGoal, LocalDate startDate,
                   LocalDate targetCompletionDate, Integer estimatedDurationWeeks,
                   Integer availableHoursPerWeek) {
        this();
        this.user = user;
        this.title = title;
        this.description = description;
        this.roadmapType = roadmapType;
        this.targetOpportunity = targetOpportunity;
        this.targetGoal = targetGoal;
        this.startDate = startDate != null ? startDate : LocalDate.now();
        this.targetCompletionDate = targetCompletionDate;
        this.estimatedDurationWeeks = estimatedDurationWeeks != null ? estimatedDurationWeeks : 4;
        this.availableHoursPerWeek = availableHoursPerWeek != null ? availableHoursPerWeek : 10;
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (startDate == null) {
            startDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void addMilestone(RoadmapMilestone milestone) {
        milestones.add(milestone);
        milestone.setRoadmap(this);
    }

    public void addTask(RoadmapTask task) {
        tasks.add(task);
        task.setRoadmap(this);
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RoadmapType getRoadmapType() {
        return roadmapType;
    }

    public void setRoadmapType(RoadmapType roadmapType) {
        this.roadmapType = roadmapType;
    }

    public Opportunity getTargetOpportunity() {
        return targetOpportunity;
    }

    public void setTargetOpportunity(Opportunity targetOpportunity) {
        this.targetOpportunity = targetOpportunity;
    }

    public String getTargetGoal() {
        return targetGoal;
    }

    public void setTargetGoal(String targetGoal) {
        this.targetGoal = targetGoal;
    }

    public RoadmapStatus getStatus() {
        return status;
    }

    public void setStatus(RoadmapStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getTargetCompletionDate() {
        return targetCompletionDate;
    }

    public void setTargetCompletionDate(LocalDate targetCompletionDate) {
        this.targetCompletionDate = targetCompletionDate;
    }

    public Integer getEstimatedDurationWeeks() {
        return estimatedDurationWeeks;
    }

    public void setEstimatedDurationWeeks(Integer estimatedDurationWeeks) {
        this.estimatedDurationWeeks = estimatedDurationWeeks;
    }

    public Integer getAvailableHoursPerWeek() {
        return availableHoursPerWeek;
    }

    public void setAvailableHoursPerWeek(Integer availableHoursPerWeek) {
        this.availableHoursPerWeek = availableHoursPerWeek;
    }

    public String getGenerationVersion() {
        return generationVersion;
    }

    public void setGenerationVersion(String generationVersion) {
        this.generationVersion = generationVersion;
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

    public List<RoadmapMilestone> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<RoadmapMilestone> milestones) {
        this.milestones = milestones;
    }

    public List<RoadmapTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<RoadmapTask> tasks) {
        this.tasks = tasks;
    }
}
