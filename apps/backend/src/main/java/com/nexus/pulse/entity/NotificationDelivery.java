package com.nexus.pulse.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_deliveries")
public class NotificationDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 32)
    private DeliveryChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @Column(name = "last_attempt_at")
    private OffsetDateTime lastAttemptAt;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public NotificationDelivery() {}

    public NotificationDelivery(UUID id, Notification notification, DeliveryChannel channel,
                                DeliveryStatus status, Integer attemptCount, OffsetDateTime lastAttemptAt,
                                OffsetDateTime deliveredAt, String failureReason, OffsetDateTime createdAt) {
        this.id = id;
        this.notification = notification;
        this.channel = channel;
        this.status = status != null ? status : DeliveryStatus.PENDING;
        this.attemptCount = attemptCount != null ? attemptCount : 0;
        this.lastAttemptAt = lastAttemptAt;
        this.deliveredAt = deliveredAt;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private Notification notification;
        private DeliveryChannel channel;
        private DeliveryStatus status = DeliveryStatus.PENDING;
        private Integer attemptCount = 0;
        private OffsetDateTime lastAttemptAt;
        private OffsetDateTime deliveredAt;
        private String failureReason;
        private OffsetDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder notification(Notification notification) { this.notification = notification; return this; }
        public Builder channel(DeliveryChannel channel) { this.channel = channel; return this; }
        public Builder status(DeliveryStatus status) { this.status = status; return this; }
        public Builder attemptCount(Integer attemptCount) { this.attemptCount = attemptCount; return this; }
        public Builder lastAttemptAt(OffsetDateTime lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; return this; }
        public Builder deliveredAt(OffsetDateTime deliveredAt) { this.deliveredAt = deliveredAt; return this; }
        public Builder failureReason(String failureReason) { this.failureReason = failureReason; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public NotificationDelivery build() {
            return new NotificationDelivery(id, notification, channel, status, attemptCount,
                    lastAttemptAt, deliveredAt, failureReason, createdAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Notification getNotification() { return notification; }
    public void setNotification(Notification notification) { this.notification = notification; }
    public DeliveryChannel getChannel() { return channel; }
    public void setChannel(DeliveryChannel channel) { this.channel = channel; }
    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
    public Integer getAttemptCount() { return attemptCount; }
    public void setAttemptCount(Integer attemptCount) { this.attemptCount = attemptCount; }
    public OffsetDateTime getLastAttemptAt() { return lastAttemptAt; }
    public void setLastAttemptAt(OffsetDateTime lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }
    public OffsetDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(OffsetDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
