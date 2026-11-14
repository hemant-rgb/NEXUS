package com.nexus.pulse.service;

import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.pulse.delivery.EmailDeliveryService;
import com.nexus.pulse.dto.*;
import com.nexus.pulse.entity.*;
import com.nexus.pulse.repository.NotificationDeliveryRepository;
import com.nexus.pulse.repository.NotificationPreferenceRepository;
import com.nexus.pulse.repository.NotificationRepository;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationDeliveryRepository deliveryRepository;
    private final UserRepository userRepository;
    private final EmailDeliveryService emailDeliveryService;

    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationPreferenceRepository preferenceRepository,
            NotificationDeliveryRepository deliveryRepository,
            UserRepository userRepository,
            EmailDeliveryService emailDeliveryService
    ) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
        this.deliveryRepository = deliveryRepository;
        this.userRepository = userRepository;
        this.emailDeliveryService = emailDeliveryService;
    }

    @Transactional
    public NotificationPreference getOrCreatePreferences(UUID userId) {
        return preferenceRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
            NotificationPreference pref = NotificationPreference.builder()
                    .user(user)
                    .inAppEnabled(true)
                    .emailEnabled(false)
                    .opportunityAlerts(true)
                    .applicationAlerts(true)
                    .roadmapAlerts(true)
                    .githubAlerts(true)
                    .agentAlerts(true)
                    .frequency("IMMEDIATE")
                    .quietHoursEnabled(false)
                    .quietHoursStart("22:00")
                    .quietHoursEnd("08:00")
                    .timezone("UTC")
                    .build();
            return preferenceRepository.save(pref);
        });
    }

    @Transactional
    public NotificationPreferenceResponse updatePreferences(UUID userId, UpdateNotificationPreferenceRequest req) {
        NotificationPreference pref = getOrCreatePreferences(userId);
        if (req.inAppEnabled() != null) pref.setInAppEnabled(req.inAppEnabled());
        if (req.emailEnabled() != null) pref.setEmailEnabled(req.emailEnabled());
        if (req.opportunityAlerts() != null) pref.setOpportunityAlerts(req.opportunityAlerts());
        if (req.applicationAlerts() != null) pref.setApplicationAlerts(req.applicationAlerts());
        if (req.roadmapAlerts() != null) pref.setRoadmapAlerts(req.roadmapAlerts());
        if (req.githubAlerts() != null) pref.setGithubAlerts(req.githubAlerts());
        if (req.agentAlerts() != null) pref.setAgentAlerts(req.agentAlerts());
        if (req.frequency() != null) pref.setFrequency(req.frequency());
        if (req.quietHoursEnabled() != null) pref.setQuietHoursEnabled(req.quietHoursEnabled());
        if (req.quietHoursStart() != null) pref.setQuietHoursStart(req.quietHoursStart());
        if (req.quietHoursEnd() != null) pref.setQuietHoursEnd(req.quietHoursEnd());
        if (req.timezone() != null) pref.setTimezone(req.timezone());
        pref = preferenceRepository.save(pref);
        return NotificationPreferenceResponse.from(pref);
    }

    @Transactional
    public Notification createNotification(
            UUID userId,
            String type,
            NotificationCategory category,
            String title,
            String message,
            NotificationPriority priority,
            String relatedEntityType,
            String relatedEntityId,
            String actionUrl,
            String idempotencyKey,
            OffsetDateTime expiresAt
    ) {
        // 1. Check idempotency
        if (idempotencyKey != null && notificationRepository.existsByUserIdAndIdempotencyKey(userId, idempotencyKey)) {
            log.info("Notification with idempotency key {} already exists for user {}. Skipping duplicate.", idempotencyKey, userId);
            return null;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // 2. Check preferences
        NotificationPreference pref = getOrCreatePreferences(userId);
        if (!isCategoryEnabled(pref, category)) {
            log.info("User {} has disabled notifications for category {}. Skipping.", userId, category);
            return null;
        }

        // 3. Persist notification
        Notification notification = Notification.builder()
                .user(user)
                .notificationType(type)
                .category(category)
                .title(title)
                .message(message)
                .priority(priority != null ? priority : NotificationPriority.NORMAL)
                .status(NotificationStatus.UNREAD)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .actionUrl(actionUrl)
                .idempotencyKey(idempotencyKey)
                .expiresAt(expiresAt)
                .build();

        notification = notificationRepository.save(notification);

        // 4. In-App delivery
        if (Boolean.TRUE.equals(pref.getInAppEnabled())) {
            NotificationDelivery inAppDelivery = NotificationDelivery.builder()
                    .notification(notification)
                    .channel(DeliveryChannel.IN_APP)
                    .status(DeliveryStatus.DELIVERED)
                    .attemptCount(1)
                    .deliveredAt(OffsetDateTime.now())
                    .lastAttemptAt(OffsetDateTime.now())
                    .build();
            deliveryRepository.save(inAppDelivery);
        }

        // 5. Email delivery (if enabled and not suppressed by quiet hours)
        if (Boolean.TRUE.equals(pref.getEmailEnabled()) && !isInQuietHours(pref)) {
            boolean sent = emailDeliveryService.sendNotificationEmail(userId, user.getEmail(), title, message);
            NotificationDelivery emailDelivery = NotificationDelivery.builder()
                    .notification(notification)
                    .channel(DeliveryChannel.EMAIL)
                    .status(sent ? DeliveryStatus.DELIVERED : DeliveryStatus.FAILED)
                    .attemptCount(1)
                    .deliveredAt(sent ? OffsetDateTime.now() : null)
                    .lastAttemptAt(OffsetDateTime.now())
                    .failureReason(sent ? null : "Email sending failed")
                    .build();
            deliveryRepository.save(emailDelivery);
        }

        return notification;
    }

    private boolean isCategoryEnabled(NotificationPreference pref, NotificationCategory category) {
        return switch (category) {
            case OPPORTUNITY -> Boolean.TRUE.equals(pref.getOpportunityAlerts());
            case APPLICATION -> Boolean.TRUE.equals(pref.getApplicationAlerts());
            case ROADMAP -> Boolean.TRUE.equals(pref.getRoadmapAlerts());
            case GITHUB -> Boolean.TRUE.equals(pref.getGithubAlerts());
            case AGENT -> Boolean.TRUE.equals(pref.getAgentAlerts());
            case SYSTEM -> true;
        };
    }

    private boolean isInQuietHours(NotificationPreference pref) {
        if (!Boolean.TRUE.equals(pref.getQuietHoursEnabled())) return false;
        try {
            ZoneId zone = ZoneId.of(pref.getTimezone() != null ? pref.getTimezone() : "UTC");
            LocalTime now = LocalTime.now(zone);
            LocalTime start = LocalTime.parse(pref.getQuietHoursStart());
            LocalTime end = LocalTime.parse(pref.getQuietHoursEnd());
            if (start.isBefore(end)) {
                return now.isAfter(start) && now.isBefore(end);
            } else {
                return now.isAfter(start) || now.isBefore(end);
            }
        } catch (Exception e) {
            log.warn("Error evaluating quiet hours for user: {}", e.getMessage());
            return false;
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(UUID userId, NotificationStatus status, NotificationCategory category, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status != null) {
            return notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageRequest)
                    .map(NotificationResponse::from);
        } else if (category != null) {
            return notificationRepository.findByUserIdAndCategoryOrderByCreatedAtDesc(userId, category, pageRequest)
                    .map(NotificationResponse::from);
        }
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageRequest)
                .map(NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(UUID userId) {
        long count = notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD);
        return new UnreadCountResponse(count);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(OffsetDateTime.now());
        return NotificationResponse.from(notificationRepository.save(notification));
    }

    @Transactional
    public NotificationResponse archiveNotification(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        notification.setStatus(NotificationStatus.ARCHIVED);
        return NotificationResponse.from(notificationRepository.save(notification));
    }

    @Transactional
    public int markAllAsRead(UUID userId) {
        return notificationRepository.markAllAsRead(userId, OffsetDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<ActivityItemResponse> getActivityFeed(UUID userId, int limit) {
        PageRequest pr = PageRequest.of(0, Math.min(limit, 50), Sort.by(Sort.Direction.DESC, "createdAt"));
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pr)
                .getContent()
                .stream()
                .map(n -> new ActivityItemResponse(
                        n.getId(),
                        n.getNotificationType(),
                        n.getCategory(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getPriority(),
                        n.getActionUrl(),
                        n.getCreatedAt()
                ))
                .toList();
    }
}
