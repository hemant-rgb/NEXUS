package com.nexus.pulse.controller;

import com.nexus.common.dto.ApiResponse;
import com.nexus.common.security.UserPrincipal;
import com.nexus.pulse.dto.*;
import com.nexus.pulse.entity.NotificationCategory;
import com.nexus.pulse.entity.NotificationStatus;
import com.nexus.pulse.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/users/me/notifications")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) NotificationCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getNotifications(principal.getId(), status, category, page, size)));
    }

    @GetMapping("/users/me/notifications/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount(principal.getId())));
    }

    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notificationService.markAsRead(principal.getId(), id)));
    }

    @PatchMapping("/notifications/{id}/archive")
    public ResponseEntity<ApiResponse<NotificationResponse>> archiveNotification(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success("Notification archived", notificationService.archiveNotification(principal.getId(), id)));
    }

    @PostMapping("/users/me/notifications/read-all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markAllAsRead(@AuthenticationPrincipal UserPrincipal principal) {
        int updated = notificationService.markAllAsRead(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("markedRead", updated, "status", "SUCCESS")));
    }

    @GetMapping("/users/me/notification-preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getPreferences(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(NotificationPreferenceResponse.from(notificationService.getOrCreatePreferences(principal.getId()))));
    }

    @PutMapping("/users/me/notification-preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> updatePreferences(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateNotificationPreferenceRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success("Preferences updated", notificationService.updatePreferences(principal.getId(), req)));
    }

    @GetMapping("/users/me/activity")
    public ResponseEntity<ApiResponse<List<ActivityItemResponse>>> getActivityFeed(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getActivityFeed(principal.getId(), limit)));
    }
}
