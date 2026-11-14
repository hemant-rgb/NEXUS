package com.nexus.github.repository;

import com.nexus.github.entity.SavedRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedRepositoryRepository extends JpaRepository<SavedRepository, UUID> {

    List<SavedRepository> findByUserIdOrderBySavedAtDesc(UUID userId);

    Optional<SavedRepository> findByUserIdAndRepositoryId(UUID userId, UUID repositoryId);

    boolean existsByUserIdAndRepositoryId(UUID userId, UUID repositoryId);

    void deleteByUserIdAndRepositoryId(UUID userId, UUID repositoryId);
}
