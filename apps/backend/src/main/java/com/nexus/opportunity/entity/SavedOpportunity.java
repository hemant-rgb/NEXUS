package com.nexus.opportunity.entity;

import com.nexus.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "saved_opportunities",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_saved_opportunity", columnNames = {"user_id", "opportunity_id"})
    }
)
public class SavedOpportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @Column(name = "saved_at", nullable = false, updatable = false)
    private Instant savedAt = Instant.now();

    @Column(columnDefinition = "TEXT")
    private String notes;

    public SavedOpportunity() {
    }

    public SavedOpportunity(User user, Opportunity opportunity, String notes) {
        this.user = user;
        this.opportunity = opportunity;
        this.notes = notes;
        this.savedAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (savedAt == null) savedAt = Instant.now();
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

    public Opportunity getOpportunity() {
        return opportunity;
    }

    public void setOpportunity(Opportunity opportunity) {
        this.opportunity = opportunity;
    }

    public Instant getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(Instant savedAt) {
        this.savedAt = savedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
