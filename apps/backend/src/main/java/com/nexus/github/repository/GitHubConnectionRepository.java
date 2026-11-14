package com.nexus.github.repository;

import com.nexus.github.entity.GitHubConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GitHubConnectionRepository extends JpaRepository<GitHubConnection, UUID> {

    Optional<GitHubConnection> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
