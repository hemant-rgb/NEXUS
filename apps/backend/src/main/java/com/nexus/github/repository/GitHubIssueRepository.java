package com.nexus.github.repository;

import com.nexus.github.entity.GitHubIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GitHubIssueRepository extends JpaRepository<GitHubIssue, UUID>, JpaSpecificationExecutor<GitHubIssue> {

    Optional<GitHubIssue> findByGithubIssueId(Long githubIssueId);

    List<GitHubIssue> findByRepositoryIdOrderByIssueNumberDesc(UUID repositoryId);
}
