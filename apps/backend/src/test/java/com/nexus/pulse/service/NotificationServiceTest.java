package com.nexus.pulse.service;

import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.pulse.delivery.EmailDeliveryService;
import com.nexus.pulse.dto.NotificationResponse;
import com.nexus.pulse.dto.UpdateNotificationPreferenceRequest;
import com.nexus.pulse.entity.*;
import com.nexus.pulse.repository.NotificationDeliveryRepository;
import com.nexus.pulse.repository.NotificationPreferenceRepository;
import com.nexus.pulse.repository.NotificationRepository;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @Mock
    private NotificationDeliveryRepository deliveryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailDeliveryService emailDeliveryService;

    @InjectMocks
    private NotificationService notificationService;

    private User testUser;
    private NotificationPreference defaultPreference;

    @BeforeEach
    void setUp() {
        testUser = new User("test@nexus.dev", "hash", "Test Developer");
        testUser.setId(UUID.randomUUID());

        defaultPreference = NotificationPreference.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .inAppEnabled(true)
                .emailEnabled(false)
                .opportunityAlerts(true)
                .applicationAlerts(true)
                .roadmapAlerts(true)
                .githubAlerts(true)
                .agentAlerts(true)
                .frequency("IMMEDIATE")
                .quietHoursEnabled(false)
                .build();
    }

    @Test
    @DisplayName("Should successfully create a notification and in-app delivery")
    void testCreateNotification_Success() {
        when(notificationRepository.existsByUserIdAndIdempotencyKey(testUser.getId(), "test-key-1")).thenReturn(false);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(preferenceRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(defaultPreference));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
            Notification n = i.getArgument(0);
            n.setId(UUID.randomUUID());
            n.setCreatedAt(OffsetDateTime.now());
            return n;
        });

        Notification result = notificationService.createNotification(
                testUser.getId(),
                "OPPORTUNITY_DEADLINE_APPROACHING",
                NotificationCategory.OPPORTUNITY,
                "Deadline Approaching",
                "3 days left to apply.",
                NotificationPriority.HIGH,
                "OPPORTUNITY",
                UUID.randomUUID().toString(),
                "/opportunities/123",
                "test-key-1",
                OffsetDateTime.now().plusDays(3)
        );

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Deadline Approaching");
        assertThat(result.getStatus()).isEqualTo(NotificationStatus.UNREAD);
        verify(deliveryRepository, times(1)).save(any(NotificationDelivery.class));
    }

    @Test
    @DisplayName("Should prevent duplicate notification when idempotency key exists")
    void testCreateNotification_DuplicateSuppressed() {
        when(notificationRepository.existsByUserIdAndIdempotencyKey(testUser.getId(), "duplicate-key")).thenReturn(true);

        Notification result = notificationService.createNotification(
                testUser.getId(),
                "OPPORTUNITY_DEADLINE_APPROACHING",
                NotificationCategory.OPPORTUNITY,
                "Deadline Approaching",
                "3 days left",
                NotificationPriority.HIGH,
                "OPPORTUNITY",
                "123",
                "/opportunities/123",
                "duplicate-key",
                null
        );

        assertThat(result).isNull();
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should suppress notification when category is disabled in user preferences")
    void testCreateNotification_CategoryDisabled() {
        defaultPreference.setOpportunityAlerts(false);
        when(notificationRepository.existsByUserIdAndIdempotencyKey(testUser.getId(), "key-opp")).thenReturn(false);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(preferenceRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(defaultPreference));

        Notification result = notificationService.createNotification(
                testUser.getId(),
                "OPPORTUNITY_DEADLINE_APPROACHING",
                NotificationCategory.OPPORTUNITY,
                "Deadline Approaching",
                "3 days left",
                NotificationPriority.HIGH,
                "OPPORTUNITY",
                "123",
                "/opportunities/123",
                "key-opp",
                null
        );

        assertThat(result).isNull();
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should mark notification as read with timestamp")
    void testMarkAsRead() {
        UUID notifId = UUID.randomUUID();
        Notification n = Notification.builder()
                .id(notifId)
                .user(testUser)
                .status(NotificationStatus.UNREAD)
                .title("Test")
                .message("Test msg")
                .category(NotificationCategory.OPPORTUNITY)
                .priority(NotificationPriority.NORMAL)
                .build();

        when(notificationRepository.findByIdAndUserId(notifId, testUser.getId())).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        NotificationResponse resp = notificationService.markAsRead(testUser.getId(), notifId);

        assertThat(resp.status()).isEqualTo(NotificationStatus.READ);
        assertThat(resp.readAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when notification belongs to another user (isolation)")
    void testMarkAsRead_MultiUserIsolation() {
        UUID foreignId = UUID.randomUUID();
        when(notificationRepository.findByIdAndUserId(foreignId, testUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(testUser.getId(), foreignId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should update user notification preferences")
    void testUpdatePreferences() {
        when(preferenceRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(defaultPreference));
        when(preferenceRepository.save(any(NotificationPreference.class))).thenAnswer(i -> i.getArgument(0));

        UpdateNotificationPreferenceRequest req = new UpdateNotificationPreferenceRequest(
                true,
                true,
                true,
                false,
                true,
                true,
                true,
                "DAILY_DIGEST",
                true,
                "23:00",
                "07:00",
                "America/New_York"
        );

        var updated = notificationService.updatePreferences(testUser.getId(), req);

        assertThat(updated.emailEnabled()).isTrue();
        assertThat(updated.applicationAlerts()).isFalse();
        assertThat(updated.frequency()).isEqualTo("DAILY_DIGEST");
        assertThat(updated.quietHoursEnabled()).isTrue();
        assertThat(updated.timezone()).isEqualTo("America/New_York");
    }
}
