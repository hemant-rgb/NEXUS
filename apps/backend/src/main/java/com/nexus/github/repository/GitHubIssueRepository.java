package com.nexus.github.repository;

import com.nexus.github.entity.GitHubIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GitHubIssueRepository extends JpaRepository<GitHubIssue, UUID> {

    Optional<GitHubIssue> findByGithubIssueId(Long githubIssueId);

    List<GitHubIssue> findByRepositoryIdOrderByIssueNumberDesc(UUID repositoryId);

    @Query("SELECT i FROM GitHubIssue i JOIN i.repository r WHERE " +
           "(:query IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(i.body) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:language IS NULL OR LOWER(r.primaryLanguage) = LOWER(:language)) AND " +
           "(:goodFirstOnly IS NULL OR :goodFirstOnly = FALSE OR i.isGoodFirstIssue = TRUE) AND " +
           "(:helpWantedOnly IS NULL OR :helpWantedOnly = FALSE OR i.isHelpWanted = TRUE) AND " +
           "(:state IS NULL OR i.state = :state) " +
           "ORDER BY i.githubCreatedAt DESC")
    List<GitHubIssue> searchIssues(
            @Param("query") String query,
            @Param("language") String language,
            @Param("goodFirstOnly") Boolean goodFirstOnly,
            @Param("helpWantedOnly") Boolean helpWantedOnly,
            @Param("state") String state
    );
}
