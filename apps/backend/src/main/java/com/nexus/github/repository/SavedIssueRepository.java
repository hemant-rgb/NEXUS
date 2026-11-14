package com.nexus.github.repository;

import com.nexus.github.entity.SavedIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedIssueRepository extends JpaRepository<SavedIssue, UUID> {

    List<SavedIssue> findByUserIdOrderBySavedAtDesc(UUID userId);

    Optional<SavedIssue> findByUserIdAndIssueId(UUID userId, UUID issueId);

    boolean existsByUserIdAndIssueId(UUID userId, UUID issueId);

    void deleteByUserIdAndIssueId(UUID userId, UUID issueId);
}
