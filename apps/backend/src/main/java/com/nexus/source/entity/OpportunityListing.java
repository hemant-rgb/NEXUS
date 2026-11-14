package com.nexus.source.entity;

import com.nexus.opportunity.entity.Opportunity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "opportunity_listings",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_source_external", columnNames = {"source_id", "external_id"})
    }
)
public class OpportunityListing {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_id", nullable = false)
    private OpportunitySource source;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "original_url", columnDefinition = "TEXT")
    private String originalUrl;

    @Column(name = "first_seen_at", nullable = false, updatable = false)
    private Instant firstSeenAt = Instant.now();

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt = Instant.now();

    @Column(name = "raw_metadata", columnDefinition = "TEXT")
    private String rawMetadata;

    public OpportunityListing() {
    }

    public OpportunityListing(
            Opportunity opportunity,
            OpportunitySource source,
            String externalId,
            String originalUrl,
            String rawMetadata) {
        this.opportunity = opportunity;
        this.source = source;
        this.externalId = externalId;
        this.originalUrl = originalUrl;
        this.rawMetadata = rawMetadata;
        this.firstSeenAt = Instant.now();
        this.lastSeenAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (firstSeenAt == null) firstSeenAt = Instant.now();
        lastSeenAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastSeenAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Opportunity getOpportunity() {
        return opportunity;
    }

    public void setOpportunity(Opportunity opportunity) {
        this.opportunity = opportunity;
    }

    public OpportunitySource getSource() {
        return source;
    }

    public void setSource(OpportunitySource source) {
        this.source = source;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public Instant getFirstSeenAt() {
        return firstSeenAt;
    }

    public void setFirstSeenAt(Instant firstSeenAt) {
        this.firstSeenAt = firstSeenAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public String getRawMetadata() {
        return rawMetadata;
    }

    public void setRawMetadata(String rawMetadata) {
        this.rawMetadata = rawMetadata;
    }
}
