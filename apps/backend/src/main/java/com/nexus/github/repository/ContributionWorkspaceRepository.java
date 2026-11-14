package com.nexus.github.repository;

import com.nexus.github.entity.ContributionStatus;
import com.nexus.github.entity.ContributionWorkspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContributionWorkspaceRepository extends JpaRepository<ContributionWorkspace, UUID> {

    List<ContributionWorkspace> findByUserIdOrderByUpdatedAtDesc(UUID userId);

    List<ContributionWorkspace> findByUserIdAndStatusOrderByUpdatedAtDesc(UUID userId, ContributionStatus status);

    Optional<ContributionWorkspace> findByIdAndUserId(UUID id, UUID userId);
}
