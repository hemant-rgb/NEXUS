package com.nexus.pulse.entity;

import com.nexus.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "notification_type", nullable = false, length = 64)
    private String notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private NotificationCategory category;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 16)
    private NotificationPriority priority = NotificationPriority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private NotificationStatus status = NotificationStatus.UNREAD;

    @Column(name = "related_entity_type", length = 64)
    private String relatedEntityType;

    @Column(name = "related_entity_id", length = 64)
    private String relatedEntityId;

    @Column(name = "action_url", length = 512)
    private String actionUrl;

    @Column(name = "idempotency_key", length = 255)
    private String idempotencyKey;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    public Notification() {}

    public Notification(UUID id, User user, String notificationType, NotificationCategory category, String title,
                        String message, NotificationPriority priority, NotificationStatus status,
                        String relatedEntityType, String relatedEntityId, String actionUrl,
                        String idempotencyKey, OffsetDateTime createdAt, OffsetDateTime readAt, OffsetDateTime expiresAt) {
        this.id = id;
        this.user = user;
        this.notificationType = notificationType;
        this.category = category;
        this.title = title;
        this.message = message;
        this.priority = priority != null ? priority : NotificationPriority.NORMAL;
        this.status = status != null ? status : NotificationStatus.UNREAD;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.actionUrl = actionUrl;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
        this.readAt = readAt;
        this.expiresAt = expiresAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private User user;
        private String notificationType;
        private NotificationCategory category;
        private String title;
        private String message;
        private NotificationPriority priority = NotificationPriority.NORMAL;
        private NotificationStatus status = NotificationStatus.UNREAD;
        private String relatedEntityType;
        private String relatedEntityId;
        private String actionUrl;
        private String idempotencyKey;
        private OffsetDateTime createdAt;
        private OffsetDateTime readAt;
        private OffsetDateTime expiresAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder notificationType(String notificationType) { this.notificationType = notificationType; return this; }
        public Builder category(NotificationCategory category) { this.category = category; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder priority(NotificationPriority priority) { this.priority = priority; return this; }
        public Builder status(NotificationStatus status) { this.status = status; return this; }
        public Builder relatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; return this; }
        public Builder relatedEntityId(String relatedEntityId) { this.relatedEntityId = relatedEntityId; return this; }
        public Builder actionUrl(String actionUrl) { this.actionUrl = actionUrl; return this; }
        public Builder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder readAt(OffsetDateTime readAt) { this.readAt = readAt; return this; }
        public Builder expiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; return this; }

        public Notification build() {
            return new Notification(id, user, notificationType, category, title, message, priority, status,
                    relatedEntityType, relatedEntityId, actionUrl, idempotencyKey, createdAt, readAt, expiresAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }
    public NotificationCategory getCategory() { return category; }
    public void setCategory(NotificationCategory category) { this.category = category; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public NotificationPriority getPriority() { return priority; }
    public void setPriority(NotificationPriority priority) { this.priority = priority; }
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; }
    public String getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(String relatedEntityId) { this.relatedEntityId = relatedEntityId; }
    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getReadAt() { return readAt; }
    public void setReadAt(OffsetDateTime readAt) { this.readAt = readAt; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }
}
