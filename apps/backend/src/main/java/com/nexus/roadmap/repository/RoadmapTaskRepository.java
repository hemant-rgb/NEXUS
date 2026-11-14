package com.nexus.roadmap.repository;

import com.nexus.roadmap.entity.RoadmapTask;
import com.nexus.roadmap.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoadmapTaskRepository extends JpaRepository<RoadmapTask, UUID> {

    List<RoadmapTask> findByRoadmapIdOrderBySequenceOrderAsc(UUID roadmapId);

    List<RoadmapTask> findByMilestoneIdOrderBySequenceOrderAsc(UUID milestoneId);

    Optional<RoadmapTask> findByIdAndRoadmapId(UUID id, UUID roadmapId);

    long countByRoadmapIdAndStatus(UUID roadmapId, TaskStatus status);

    long countByMilestoneIdAndStatus(UUID milestoneId, TaskStatus status);

    @Query("SELECT t FROM RoadmapTask t WHERE t.roadmap.id = :roadmapId AND t.status != 'COMPLETED' AND t.status != 'SKIPPED' AND t.scheduledDate < :date ORDER BY t.scheduledDate ASC")
    List<RoadmapTask> findOverdueTasksByRoadmapId(@Param("roadmapId") UUID roadmapId, @Param("date") LocalDate date);

    @Query("SELECT t FROM RoadmapTask t WHERE t.roadmap.user.id = :userId AND t.status != 'COMPLETED' AND t.status != 'SKIPPED' AND t.scheduledDate < :date ORDER BY t.scheduledDate ASC")
    List<RoadmapTask> findOverdueTasksByUserId(@Param("userId") UUID userId, @Param("date") LocalDate date);

    @Query("SELECT t FROM RoadmapTask t WHERE t.roadmap.user.id = :userId AND t.status != 'COMPLETED' AND t.status != 'SKIPPED' AND t.scheduledDate >= :today AND t.scheduledDate <= :futureDate ORDER BY t.scheduledDate ASC")
    List<RoadmapTask> findUpcomingTasksByUserId(@Param("userId") UUID userId, @Param("today") LocalDate today, @Param("futureDate") LocalDate futureDate);
}
