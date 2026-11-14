package com.nexus.application.repository;

import com.nexus.application.entity.Application;
import com.nexus.application.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    List<Application> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Application> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, ApplicationStatus status);

    Optional<Application> findByIdAndUserId(UUID id, UUID userId);

    long countByUserIdAndStatus(UUID userId, ApplicationStatus status);

    long countByUserId(UUID userId);

    @Query("SELECT a FROM Application a WHERE a.user.id = :userId AND a.deadline >= :startDate AND a.deadline <= :endDate ORDER BY a.deadline ASC")
    List<Application> findUpcomingDeadlines(@Param("userId") UUID userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
