package com.nexus.matching.repository;

import com.nexus.matching.entity.OpportunityMatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OpportunityMatchRepository extends JpaRepository<OpportunityMatch, UUID>, JpaSpecificationExecutor<OpportunityMatch> {

    Optional<OpportunityMatch> findByUserIdAndOpportunityId(UUID userId, UUID opportunityId);

    List<OpportunityMatch> findTop5ByUserIdOrderByOverallScoreDesc(UUID userId);

    @Query("SELECT m FROM OpportunityMatch m JOIN FETCH m.opportunity WHERE m.user.id = :userId ORDER BY m.overallScore DESC")
    List<OpportunityMatch> findAllByUserIdWithOpportunity(@Param("userId") UUID userId);

    long countByUserId(UUID userId);

    void deleteAllByUserId(UUID userId);
}
