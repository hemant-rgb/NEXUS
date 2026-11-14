package com.nexus.roadmap.repository;

import com.nexus.roadmap.entity.LearningResource;
import com.nexus.roadmap.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LearningResourceRepository extends JpaRepository<LearningResource, UUID> {

    List<LearningResource> findBySkillNameIgnoreCase(String skillName);

    List<LearningResource> findByVerificationStatus(VerificationStatus status);

    @Query("SELECT r FROM LearningResource r WHERE LOWER(r.skillName) = LOWER(:skillName) OR LOWER(r.title) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY r.verificationStatus ASC, r.title ASC")
    List<LearningResource> searchResources(@Param("skillName") String skillName, @Param("query") String query);
}
