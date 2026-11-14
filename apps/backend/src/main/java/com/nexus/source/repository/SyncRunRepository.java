package com.nexus.source.repository;

import com.nexus.source.entity.SyncRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SyncRunRepository extends JpaRepository<SyncRun, UUID> {
    List<SyncRun> findTop10BySourceIdOrderByStartedAtDesc(UUID sourceId);
    Page<SyncRun> findBySourceIdOrderByStartedAtDesc(UUID sourceId, Pageable pageable);
}
