package com.nexus.pulse.repository;

import com.nexus.pulse.entity.Notification;
import com.nexus.pulse.entity.NotificationCategory;
import com.nexus.pulse.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {

    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);

    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, NotificationStatus status, Pageable pageable);

    Page<Notification> findByUserIdAndCategoryOrderByCreatedAtDesc(UUID userId, NotificationCategory category, Pageable pageable);

    long countByUserIdAndStatus(UUID userId, NotificationStatus status);

    boolean existsByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = :now WHERE n.user.id = :userId AND n.status = 'UNREAD'")
    int markAllAsRead(@Param("userId") UUID userId, @Param("now") OffsetDateTime now);
}
