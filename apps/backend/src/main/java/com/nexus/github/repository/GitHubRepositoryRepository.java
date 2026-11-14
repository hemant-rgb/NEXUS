package com.nexus.github.repository;

import com.nexus.github.entity.GitHubRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GitHubRepositoryRepository extends JpaRepository<GitHubRepository, UUID> {

    Optional<GitHubRepository> findByGithubRepoId(Long githubRepoId);

    Optional<GitHubRepository> findByFullNameIgnoreCase(String fullName);

    @Query("SELECT r FROM GitHubRepository r WHERE " +
           "(:query IS NULL OR LOWER(r.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:language IS NULL OR LOWER(r.primaryLanguage) = LOWER(:language)) " +
           "ORDER BY r.starsCount DESC")
    List<GitHubRepository> searchRepositories(@Param("query") String query, @Param("language") String language);
}
