package com.nexus.roadmap.repository;

import com.nexus.roadmap.entity.Roadmap;
import com.nexus.roadmap.entity.RoadmapStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, UUID> {

    List<Roadmap> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Roadmap> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, RoadmapStatus status);

    @Query("SELECT r FROM Roadmap r LEFT JOIN FETCH r.milestones m LEFT JOIN FETCH r.tasks t WHERE r.id = :id")
    Optional<Roadmap> findByIdWithDetails(@Param("id") UUID id);

    Optional<Roadmap> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT r FROM Roadmap r LEFT JOIN FETCH r.milestones m LEFT JOIN FETCH r.tasks t WHERE r.id = :id AND r.user.id = :userId")
    Optional<Roadmap> findByIdAndUserIdWithDetails(@Param("id") UUID id, @Param("userId") UUID userId);

    long countByUserIdAndStatus(UUID userId, RoadmapStatus status);
}
