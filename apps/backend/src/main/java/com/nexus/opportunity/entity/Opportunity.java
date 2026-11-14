package com.nexus.opportunity.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "opportunities")
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String organization;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "opportunity_type", nullable = false, length = 50)
    private String opportunityType;

    private String location;

    @Column(name = "country_code", length = 10)
    private String countryCode;

    @Column(name = "remote_available", nullable = false)
    private boolean remoteAvailable = false;

    @Column(name = "eligibility_criteria", columnDefinition = "TEXT")
    private String eligibilityCriteria;

    @Column(name = "required_skills", columnDefinition = "TEXT")
    private String requiredSkills; // JSON array string

    @Column(name = "preferred_skills", columnDefinition = "TEXT")
    private String preferredSkills; // JSON array string

    @Column(name = "application_url", columnDefinition = "TEXT", nullable = false)
    private String applicationUrl;

    @Column(name = "official_website_url", columnDefinition = "TEXT")
    private String officialWebsiteUrl;

    @Column
    private LocalDate deadline;

    @Column(name = "deadline_type", nullable = false, length = 50)
    private String deadlineType = "STRICT";

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "OPEN";

    @Column(name = "canonical_url", columnDefinition = "TEXT")
    private String canonicalUrl;

    @Column(name = "first_discovered_at", nullable = false, updatable = false)
    private Instant firstDiscoveredAt = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "last_verified_at", nullable = false)
    private Instant lastVerifiedAt = Instant.now();

    public Opportunity() {
    }

    public Opportunity(String title, String organization, String description, String category, String opportunityType) {
        this(title, organization, description, category, opportunityType, "Remote", "GLOBAL", true,
                "All", "[]", "[]", "https://nexus.dev", "https://nexus.dev", null, "ROLLING", null, null, "OPEN");
    }

    public Opportunity(
            String title,
            String organization,
            String description,
            String category,
            String opportunityType,
            String location,
            String countryCode,
            boolean remoteAvailable,
            String eligibilityCriteria,
            String requiredSkills,
            String preferredSkills,
            String applicationUrl,
            String officialWebsiteUrl,
            LocalDate deadline,
            String deadlineType,
            LocalDate startDate,
            LocalDate endDate,
            String status) {
        this.title = title;
        this.organization = organization;
        this.description = description;
        this.category = category;
        this.opportunityType = opportunityType != null ? opportunityType : "OTHER";
        this.location = location;
        this.countryCode = countryCode;
        this.remoteAvailable = remoteAvailable;
        this.eligibilityCriteria = eligibilityCriteria;
        this.requiredSkills = requiredSkills;
        this.preferredSkills = preferredSkills;
        this.applicationUrl = applicationUrl;
        this.officialWebsiteUrl = officialWebsiteUrl;
        this.deadline = deadline;
        this.deadlineType = deadlineType != null ? deadlineType : "STRICT";
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status != null ? status : "OPEN";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.lastVerifiedAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        updatedAt = Instant.now();
        if (lastVerifiedAt == null) lastVerifiedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getOpportunityType() {
        return opportunityType;
    }

    public void setOpportunityType(String opportunityType) {
        this.opportunityType = opportunityType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public boolean isRemoteAvailable() {
        return remoteAvailable;
    }

    public void setRemoteAvailable(boolean remoteAvailable) {
        this.remoteAvailable = remoteAvailable;
    }

    public String getEligibilityCriteria() {
        return eligibilityCriteria;
    }

    public void setEligibilityCriteria(String eligibilityCriteria) {
        this.eligibilityCriteria = eligibilityCriteria;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public String getPreferredSkills() {
        return preferredSkills;
    }

    public void setPreferredSkills(String preferredSkills) {
        this.preferredSkills = preferredSkills;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    public String getOfficialWebsiteUrl() {
        return officialWebsiteUrl;
    }

    public void setOfficialWebsiteUrl(String officialWebsiteUrl) {
        this.officialWebsiteUrl = officialWebsiteUrl;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getDeadlineType() {
        return deadlineType;
    }

    public void setDeadlineType(String deadlineType) {
        this.deadlineType = deadlineType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    public void setCanonicalUrl(String canonicalUrl) {
        this.canonicalUrl = canonicalUrl;
    }

    public Instant getFirstDiscoveredAt() {
        return firstDiscoveredAt;
    }

    public void setFirstDiscoveredAt(Instant firstDiscoveredAt) {
        this.firstDiscoveredAt = firstDiscoveredAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getLastVerifiedAt() {
        return lastVerifiedAt;
    }

    public void setLastVerifiedAt(Instant lastVerifiedAt) {
        this.lastVerifiedAt = lastVerifiedAt;
    }
}
