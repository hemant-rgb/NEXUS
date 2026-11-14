package com.nexus.opportunity.repository;

import com.nexus.opportunity.entity.SavedOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedOpportunityRepository extends JpaRepository<SavedOpportunity, UUID> {

    @Query("SELECT s FROM SavedOpportunity s JOIN FETCH s.opportunity WHERE s.user.id = :userId ORDER BY s.savedAt DESC")
    List<SavedOpportunity> findByUserIdWithOpportunity(@Param("userId") UUID userId);

    @Query("SELECT s FROM SavedOpportunity s JOIN FETCH s.opportunity o JOIN FETCH s.user u WHERE o.deadline IS NOT NULL AND o.deadline >= :today AND o.deadline <= :closingDate")
    List<SavedOpportunity> findClosingSoon(@Param("today") java.time.LocalDate today, @Param("closingDate") java.time.LocalDate closingDate);

    Optional<SavedOpportunity> findByUserIdAndOpportunityId(UUID userId, UUID opportunityId);

    boolean existsByUserIdAndOpportunityId(UUID userId, UUID opportunityId);

    void deleteByUserIdAndOpportunityId(UUID userId, UUID opportunityId);
}
