package com.nexus.source.repository;

import com.nexus.source.entity.OpportunitySource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OpportunitySourceRepository extends JpaRepository<OpportunitySource, UUID> {
    Optional<OpportunitySource> findBySourceKey(String sourceKey);
    List<OpportunitySource> findAllByEnabledTrue();
    boolean existsBySourceKey(String sourceKey);
}
