package com.nexus.pulse.entity;

import com.nexus.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "in_app_enabled", nullable = false)
    private Boolean inAppEnabled = true;

    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = false;

    @Column(name = "opportunity_alerts", nullable = false)
    private Boolean opportunityAlerts = true;

    @Column(name = "application_alerts", nullable = false)
    private Boolean applicationAlerts = true;

    @Column(name = "roadmap_alerts", nullable = false)
    private Boolean roadmapAlerts = true;

    @Column(name = "github_alerts", nullable = false)
    private Boolean githubAlerts = true;

    @Column(name = "agent_alerts", nullable = false)
    private Boolean agentAlerts = true;

    @Column(name = "frequency", nullable = false, length = 32)
    private String frequency = "IMMEDIATE";

    @Column(name = "quiet_hours_enabled", nullable = false)
    private Boolean quietHoursEnabled = false;

    @Column(name = "quiet_hours_start", nullable = false, length = 10)
    private String quietHoursStart = "22:00";

    @Column(name = "quiet_hours_end", nullable = false, length = 10)
    private String quietHoursEnd = "08:00";

    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone = "UTC";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public NotificationPreference() {}

    public NotificationPreference(UUID id, User user, Boolean inAppEnabled, Boolean emailEnabled,
                                  Boolean opportunityAlerts, Boolean applicationAlerts,
                                  Boolean roadmapAlerts, Boolean githubAlerts, Boolean agentAlerts,
                                  String frequency, Boolean quietHoursEnabled, String quietHoursStart,
                                  String quietHoursEnd, String timezone, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.inAppEnabled = inAppEnabled != null ? inAppEnabled : true;
        this.emailEnabled = emailEnabled != null ? emailEnabled : false;
        this.opportunityAlerts = opportunityAlerts != null ? opportunityAlerts : true;
        this.applicationAlerts = applicationAlerts != null ? applicationAlerts : true;
        this.roadmapAlerts = roadmapAlerts != null ? roadmapAlerts : true;
        this.githubAlerts = githubAlerts != null ? githubAlerts : true;
        this.agentAlerts = agentAlerts != null ? agentAlerts : true;
        this.frequency = frequency != null ? frequency : "IMMEDIATE";
        this.quietHoursEnabled = quietHoursEnabled != null ? quietHoursEnabled : false;
        this.quietHoursStart = quietHoursStart != null ? quietHoursStart : "22:00";
        this.quietHoursEnd = quietHoursEnd != null ? quietHoursEnd : "08:00";
        this.timezone = timezone != null ? timezone : "UTC";
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private User user;
        private Boolean inAppEnabled = true;
        private Boolean emailEnabled = false;
        private Boolean opportunityAlerts = true;
        private Boolean applicationAlerts = true;
        private Boolean roadmapAlerts = true;
        private Boolean githubAlerts = true;
        private Boolean agentAlerts = true;
        private String frequency = "IMMEDIATE";
        private Boolean quietHoursEnabled = false;
        private String quietHoursStart = "22:00";
        private String quietHoursEnd = "08:00";
        private String timezone = "UTC";
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder inAppEnabled(Boolean inAppEnabled) { this.inAppEnabled = inAppEnabled; return this; }
        public Builder emailEnabled(Boolean emailEnabled) { this.emailEnabled = emailEnabled; return this; }
        public Builder opportunityAlerts(Boolean opportunityAlerts) { this.opportunityAlerts = opportunityAlerts; return this; }
        public Builder applicationAlerts(Boolean applicationAlerts) { this.applicationAlerts = applicationAlerts; return this; }
        public Builder roadmapAlerts(Boolean roadmapAlerts) { this.roadmapAlerts = roadmapAlerts; return this; }
        public Builder githubAlerts(Boolean githubAlerts) { this.githubAlerts = githubAlerts; return this; }
        public Builder agentAlerts(Boolean agentAlerts) { this.agentAlerts = agentAlerts; return this; }
        public Builder frequency(String frequency) { this.frequency = frequency; return this; }
        public Builder quietHoursEnabled(Boolean quietHoursEnabled) { this.quietHoursEnabled = quietHoursEnabled; return this; }
        public Builder quietHoursStart(String quietHoursStart) { this.quietHoursStart = quietHoursStart; return this; }
        public Builder quietHoursEnd(String quietHoursEnd) { this.quietHoursEnd = quietHoursEnd; return this; }
        public Builder timezone(String timezone) { this.timezone = timezone; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public NotificationPreference build() {
            return new NotificationPreference(id, user, inAppEnabled, emailEnabled, opportunityAlerts,
                    applicationAlerts, roadmapAlerts, githubAlerts, agentAlerts, frequency,
                    quietHoursEnabled, quietHoursStart, quietHoursEnd, timezone, createdAt, updatedAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Boolean getInAppEnabled() { return inAppEnabled; }
    public void setInAppEnabled(Boolean inAppEnabled) { this.inAppEnabled = inAppEnabled; }
    public Boolean getEmailEnabled() { return emailEnabled; }
    public void setEmailEnabled(Boolean emailEnabled) { this.emailEnabled = emailEnabled; }
    public Boolean getOpportunityAlerts() { return opportunityAlerts; }
    public void setOpportunityAlerts(Boolean opportunityAlerts) { this.opportunityAlerts = opportunityAlerts; }
    public Boolean getApplicationAlerts() { return applicationAlerts; }
    public void setApplicationAlerts(Boolean applicationAlerts) { this.applicationAlerts = applicationAlerts; }
    public Boolean getRoadmapAlerts() { return roadmapAlerts; }
    public void setRoadmapAlerts(Boolean roadmapAlerts) { this.roadmapAlerts = roadmapAlerts; }
    public Boolean getGithubAlerts() { return githubAlerts; }
    public void setGithubAlerts(Boolean githubAlerts) { this.githubAlerts = githubAlerts; }
    public Boolean getAgentAlerts() { return agentAlerts; }
    public void setAgentAlerts(Boolean agentAlerts) { this.agentAlerts = agentAlerts; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public Boolean getQuietHoursEnabled() { return quietHoursEnabled; }
    public void setQuietHoursEnabled(Boolean quietHoursEnabled) { this.quietHoursEnabled = quietHoursEnabled; }
    public String getQuietHoursStart() { return quietHoursStart; }
    public void setQuietHoursStart(String quietHoursStart) { this.quietHoursStart = quietHoursStart; }
    public String getQuietHoursEnd() { return quietHoursEnd; }
    public void setQuietHoursEnd(String quietHoursEnd) { this.quietHoursEnd = quietHoursEnd; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
