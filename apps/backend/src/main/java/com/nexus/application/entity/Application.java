package com.nexus.application.entity;

import com.nexus.opportunity.entity.Opportunity;
import com.nexus.roadmap.entity.Roadmap;
import com.nexus.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id")
    private Opportunity opportunity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id")
    private Roadmap roadmap;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String organization;

    @Column(name = "application_type", nullable = false, length = 100)
    private String applicationType; // INTERNSHIP, FULL_TIME, GSOC, OPEN_SOURCE, etc.

    @Column(name = "application_url", columnDefinition = "TEXT")
    private String applicationUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ApplicationStatus status = ApplicationStatus.PREPARING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ApplicationPriority priority = ApplicationPriority.MEDIUM;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "applied_at")
    private OffsetDateTime appliedAt;

    @Column(name = "expected_response_date")
    private LocalDate expectedResponseDate;

    @Column(name = "user_notes", columnDefinition = "TEXT")
    private String userNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<ApplicationActivity> activities = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<ApplicationDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("scheduledAt ASC")
    private List<ApplicationInterview> interviews = new ArrayList<>();

    public Application() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Application(User user, Opportunity opportunity, Roadmap roadmap, String title,
                       String organization, String applicationType, ApplicationStatus status,
                       ApplicationPriority priority, LocalDate deadline) {
        this();
        this.user = user;
        this.opportunity = opportunity;
        this.roadmap = roadmap;
        this.title = title;
        this.organization = organization;
        this.applicationType = applicationType != null ? applicationType : "INTERNSHIP";
        this.status = status != null ? status : ApplicationStatus.PREPARING;
        this.priority = priority != null ? priority : ApplicationPriority.MEDIUM;
        this.deadline = deadline;
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

    public Opportunity getOpportunity() {
        return opportunity;
    }

    public void setOpportunity(Opportunity opportunity) {
        this.opportunity = opportunity;
    }

    public Roadmap getRoadmap() {
        return roadmap;
    }

    public void setRoadmap(Roadmap roadmap) {
        this.roadmap = roadmap;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public ApplicationPriority getPriority() {
        return priority;
    }

    public void setPriority(ApplicationPriority priority) {
        this.priority = priority;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public OffsetDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(OffsetDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public LocalDate getExpectedResponseDate() {
        return expectedResponseDate;
    }

    public void setExpectedResponseDate(LocalDate expectedResponseDate) {
        this.expectedResponseDate = expectedResponseDate;
    }

    public String getUserNotes() {
        return userNotes;
    }

    public void setUserNotes(String userNotes) {
        this.userNotes = userNotes;
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

    public List<ApplicationActivity> getActivities() {
        return activities;
    }

    public void setActivities(List<ApplicationActivity> activities) {
        this.activities = activities;
    }

    public List<ApplicationDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<ApplicationDocument> documents) {
        this.documents = documents;
    }

    public List<ApplicationInterview> getInterviews() {
        return interviews;
    }

    public void setInterviews(List<ApplicationInterview> interviews) {
        this.interviews = interviews;
    }
}
