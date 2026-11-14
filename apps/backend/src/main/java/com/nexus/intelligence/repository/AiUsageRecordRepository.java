package com.nexus.intelligence.repository;

import com.nexus.intelligence.entity.AiUsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AiUsageRecordRepository extends JpaRepository<AiUsageRecord, UUID> {
    long countByUserId(UUID userId);
}
