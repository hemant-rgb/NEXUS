package com.nexus.application.repository;

import com.nexus.application.entity.ApplicationInterview;
import com.nexus.application.entity.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationInterviewRepository extends JpaRepository<ApplicationInterview, UUID> {

    List<ApplicationInterview> findByApplicationIdOrderByScheduledAtAsc(UUID applicationId);

    Optional<ApplicationInterview> findByIdAndApplicationId(UUID id, UUID applicationId);

    @Query("SELECT i FROM ApplicationInterview i WHERE i.application.user.id = :userId AND i.status = :status ORDER BY i.scheduledAt ASC")
    List<ApplicationInterview> findUpcomingByUserId(@Param("userId") UUID userId, @Param("status") InterviewStatus status);

    @Query("SELECT i FROM ApplicationInterview i JOIN FETCH i.application a JOIN FETCH a.user u WHERE i.scheduledAt >= :startDate AND i.scheduledAt <= :endDate AND i.status = 'SCHEDULED' ORDER BY i.scheduledAt ASC")
    List<ApplicationInterview> findUpcomingScheduled(@Param("startDate") java.time.OffsetDateTime startDate, @Param("endDate") java.time.OffsetDateTime endDate);
}
