package com.nexus.github.repository;

import com.nexus.github.entity.GitHubRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GitHubRepositoryRepository extends JpaRepository<GitHubRepository, UUID>, JpaSpecificationExecutor<GitHubRepository> {

    Optional<GitHubRepository> findByGithubRepoId(Long githubRepoId);

    Optional<GitHubRepository> findByFullNameIgnoreCase(String fullName);
}
