package com.nexus.opportunity.repository;

import com.nexus.opportunity.entity.Opportunity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, UUID>, JpaSpecificationExecutor<Opportunity> {

    Optional<Opportunity> findByTitleIgnoreCaseAndOrganizationIgnoreCase(String title, String organization);

    Optional<Opportunity> findByCanonicalUrl(String canonicalUrl);

    @Query("SELECT o FROM Opportunity o WHERE o.status = 'OPEN' ORDER BY o.createdAt DESC")
    List<Opportunity> findFeatured(Pageable pageable);

    @Query("SELECT o.category, COUNT(o) FROM Opportunity o WHERE o.status = 'OPEN' GROUP BY o.category")
    List<Object[]> countOpportunitiesByCategory();
}
