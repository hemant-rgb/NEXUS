package com.nexus.skills.repository;

import com.nexus.skills.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, UUID> {

    @Query("SELECT us FROM UserSkill us JOIN FETCH us.skill WHERE us.user.id = :userId ORDER BY us.createdAt ASC")
    List<UserSkill> findByUserIdWithSkill(@Param("userId") UUID userId);

    Optional<UserSkill> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndSkillId(UUID userId, UUID skillId);

    void deleteByIdAndUserId(UUID id, UUID userId);
}
