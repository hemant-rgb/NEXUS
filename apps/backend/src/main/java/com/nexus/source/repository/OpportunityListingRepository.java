package com.nexus.source.repository;

import com.nexus.source.entity.OpportunityListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OpportunityListingRepository extends JpaRepository<OpportunityListing, UUID> {
    Optional<OpportunityListing> findBySourceIdAndExternalId(UUID sourceId, String externalId);
    boolean existsBySourceIdAndExternalId(UUID sourceId, String externalId);
    List<OpportunityListing> findByOpportunityId(UUID opportunityId);
}
