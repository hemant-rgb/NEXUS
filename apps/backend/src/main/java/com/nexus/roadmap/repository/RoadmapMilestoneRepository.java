package com.nexus.roadmap.repository;

import com.nexus.roadmap.entity.RoadmapMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoadmapMilestoneRepository extends JpaRepository<RoadmapMilestone, UUID> {

    List<RoadmapMilestone> findByRoadmapIdOrderBySequenceOrderAsc(UUID roadmapId);

    Optional<RoadmapMilestone> findByIdAndRoadmapId(UUID id, UUID roadmapId);
}
