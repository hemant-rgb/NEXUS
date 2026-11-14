package com.nexus.intelligence.repository;

import com.nexus.intelligence.entity.OpportunityAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OpportunityAnalysisRepository extends JpaRepository<OpportunityAnalysis, UUID> {

    Optional<OpportunityAnalysis> findByUserIdAndOpportunityId(UUID userId, UUID opportunityId);

    void deleteAllByUserId(UUID userId);
}
