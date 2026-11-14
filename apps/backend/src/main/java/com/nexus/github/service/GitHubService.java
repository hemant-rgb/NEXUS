package com.nexus.github.service;

import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.github.config.TokenEncryptionService;
import com.nexus.github.dto.*;
import com.nexus.github.entity.*;
import com.nexus.github.repository.*;
import com.nexus.roadmap.entity.RoadmapTask;
import com.nexus.roadmap.repository.RoadmapTaskRepository;
import com.nexus.skills.entity.UserSkill;
import com.nexus.skills.repository.UserSkillRepository;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@Transactional
public class GitHubService {

    private final GitHubConnectionRepository connectionRepository;
    private final GitHubRepositoryRepository repositoryRepository;
    private final SavedRepositoryRepository savedRepositoryRepository;
    private final GitHubIssueRepository issueRepository;
    private final SavedIssueRepository savedIssueRepository;
    private final ContributionWorkspaceRepository workspaceRepository;
    private final TokenEncryptionService encryptionService;
    private final GitHubClient githubClient;
    private final IssueSuitabilityService suitabilityService;
    private final GitHubAiIntelligenceService aiIntelligenceService;
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final RoadmapTaskRepository roadmapTaskRepository;

    public GitHubService(
            GitHubConnectionRepository connectionRepository,
            GitHubRepositoryRepository repositoryRepository,
            SavedRepositoryRepository savedRepositoryRepository,
            GitHubIssueRepository issueRepository,
            SavedIssueRepository savedIssueRepository,
            ContributionWorkspaceRepository workspaceRepository,
            TokenEncryptionService encryptionService,
            GitHubClient githubClient,
            IssueSuitabilityService suitabilityService,
            GitHubAiIntelligenceService aiIntelligenceService,
            UserRepository userRepository,
            UserSkillRepository userSkillRepository,
            RoadmapTaskRepository roadmapTaskRepository
    ) {
        this.connectionRepository = connectionRepository;
        this.repositoryRepository = repositoryRepository;
        this.savedRepositoryRepository = savedRepositoryRepository;
        this.issueRepository = issueRepository;
        this.savedIssueRepository = savedIssueRepository;
        this.workspaceRepository = workspaceRepository;
        this.encryptionService = encryptionService;
        this.githubClient = githubClient;
        this.suitabilityService = suitabilityService;
        this.aiIntelligenceService = aiIntelligenceService;
        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
        this.roadmapTaskRepository = roadmapTaskRepository;
    }

    @Transactional(readOnly = true)
    public Optional<GitHubConnectionResponse> getConnection(UUID userId) {
        return connectionRepository.findByUserId(userId).map(GitHubConnectionResponse::from);
    }

    public GitHubConnectionResponse connect(UUID userId, ConnectGitHubRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        String rawToken = request.accessToken();
        String encryptedToken = encryptionService.encrypt(rawToken);

        // Try to fetch remote profile from GitHub API if live
        Optional<GitHubClient.GitHubUserProfile> remoteProfile = githubClient.fetchAuthenticatedUserProfile(rawToken);

        Long githubUserId = remoteProfile.map(GitHubClient.GitHubUserProfile::id).orElse(100000L + Math.abs(userId.hashCode() % 900000L));
        String username = remoteProfile.map(GitHubClient.GitHubUserProfile::login).orElse("dev-" + userId.toString().substring(0, 8));
        String avatarUrl = remoteProfile.map(GitHubClient.GitHubUserProfile::avatarUrl).orElse("https://avatars.githubusercontent.com/u/" + githubUserId);
        String profileUrl = remoteProfile.map(GitHubClient.GitHubUserProfile::htmlUrl).orElse("https://github.com/" + username);
        Integer publicRepos = remoteProfile.map(GitHubClient.GitHubUserProfile::publicRepos).orElse(12);
        Integer followers = remoteProfile.map(GitHubClient.GitHubUserProfile::followers).orElse(5);

        String scopes = request.scopes() != null ? request.scopes() : "read:user,repo";

        GitHubConnection connection = connectionRepository.findByUserId(userId)
                .orElseGet(() -> new GitHubConnection(user, String.valueOf(githubUserId), username, avatarUrl, profileUrl, encryptedToken, scopes));

        connection.setGithubUserId(String.valueOf(githubUserId));
        connection.setGithubUsername(username);
        connection.setAvatarUrl(avatarUrl);
        connection.setProfileUrl(profileUrl);
        connection.setPublicReposCount(publicRepos);
        connection.setFollowersCount(followers);
        connection.setEncryptedAccessToken(encryptedToken);
        connection.setTokenScope(scopes);
        connection.setLastSyncedAt(OffsetDateTime.now(ZoneOffset.UTC));

        GitHubConnection saved = connectionRepository.save(connection);
        return GitHubConnectionResponse.from(saved);
    }

    public void disconnect(UUID userId) {
        connectionRepository.findByUserId(userId).ifPresent(connectionRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<GitHubRepoResponse> searchRepositories(UUID userId, String query, String language) {
        Specification<GitHubRepository> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.trim().toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("fullName")), pattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(nameLike, descLike));
            }
            if (language != null && !language.isBlank() && !"ALL".equalsIgnoreCase(language)) {
                predicates.add(cb.equal(cb.lower(root.get("primaryLanguage")), language.trim().toLowerCase()));
            }
            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };

        List<GitHubRepository> repos = repositoryRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "starsCount"));
        Set<UUID> savedRepoIds = new HashSet<>();
        if (userId != null) {
            savedRepositoryRepository.findByUserIdOrderBySavedAtDesc(userId)
                    .forEach(sr -> savedRepoIds.add(sr.getRepository().getId()));
        }

        return repos.stream()
                .map(r -> GitHubRepoResponse.from(r, savedRepoIds.contains(r.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public GitHubRepoResponse getRepository(UUID userId, UUID repositoryId) {
        GitHubRepository repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("GitHub repository not found: " + repositoryId));
        boolean isSaved = userId != null && savedRepositoryRepository.existsByUserIdAndRepositoryId(userId, repositoryId);
        return GitHubRepoResponse.from(repo, isSaved);
    }

    public void saveRepository(UUID userId, UUID repositoryId) {
        if (savedRepositoryRepository.existsByUserIdAndRepositoryId(userId, repositoryId)) {
            return;
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        GitHubRepository repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found: " + repositoryId));

        savedRepositoryRepository.save(new SavedRepository(user, repo));
    }

    public void unsaveRepository(UUID userId, UUID repositoryId) {
        savedRepositoryRepository.deleteByUserIdAndRepositoryId(userId, repositoryId);
    }

    @Transactional(readOnly = true)
    public List<GitHubRepoResponse> getSavedRepositories(UUID userId) {
        List<SavedRepository> saved = savedRepositoryRepository.findByUserIdOrderBySavedAtDesc(userId);
        return saved.stream()
                .map(s -> GitHubRepoResponse.from(s.getRepository(), true))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GitHubIssueResponse> searchIssues(
            UUID userId, String query, String language, Boolean goodFirstOnly, Boolean helpWantedOnly, String state) {

        Specification<GitHubIssue> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                Predicate bodyLike = cb.like(cb.lower(root.get("body")), pattern);
                predicates.add(cb.or(titleLike, bodyLike));
            }
            if (language != null && !language.isBlank() && !"ALL".equalsIgnoreCase(language)) {
                predicates.add(cb.equal(cb.lower(root.get("repository").get("primaryLanguage")), language.trim().toLowerCase()));
            }
            if (Boolean.TRUE.equals(goodFirstOnly)) {
                predicates.add(cb.isTrue(root.get("isGoodFirstIssue")));
            }
            if (Boolean.TRUE.equals(helpWantedOnly)) {
                predicates.add(cb.isTrue(root.get("isHelpWanted")));
            }
            if (state != null && !state.isBlank() && !"ALL".equalsIgnoreCase(state)) {
                predicates.add(cb.equal(cb.upper(root.get("state")), state.trim().toUpperCase()));
            }
            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };

        List<GitHubIssue> issues = issueRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "githubCreatedAt"));

        Set<UUID> savedIssueIds = new HashSet<>();
        List<UserSkill> userSkills = Collections.emptyList();
        if (userId != null) {
            savedIssueRepository.findByUserIdOrderBySavedAtDesc(userId)
                    .forEach(si -> savedIssueIds.add(si.getIssue().getId()));
            userSkills = userSkillRepository.findByUserIdWithSkill(userId);
        }

        final List<UserSkill> finalSkills = userSkills;
        return issues.stream()
                .map(issue -> {
                    Double score = null;
                    if (userId != null) {
                        score = suitabilityService.evaluateSuitability(issue, finalSkills).suitabilityScore();
                    }
                    return GitHubIssueResponse.from(issue, savedIssueIds.contains(issue.getId()), score);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public GitHubIssueResponse getIssue(UUID userId, UUID issueId) {
        GitHubIssue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("GitHub issue not found: " + issueId));
        boolean isSaved = userId != null && savedIssueRepository.existsByUserIdAndIssueId(userId, issueId);

        Double score = null;
        if (userId != null) {
            List<UserSkill> skills = userSkillRepository.findByUserIdWithSkill(userId);
            score = suitabilityService.evaluateSuitability(issue, skills).suitabilityScore();
        }
        return GitHubIssueResponse.from(issue, isSaved, score);
    }

    public void saveIssue(UUID userId, UUID issueId) {
        if (savedIssueRepository.existsByUserIdAndIssueId(userId, issueId)) {
            return;
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        GitHubIssue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));

        savedIssueRepository.save(new SavedIssue(user, issue));
    }

    public void unsaveIssue(UUID userId, UUID issueId) {
        savedIssueRepository.deleteByUserIdAndIssueId(userId, issueId);
    }

    @Transactional(readOnly = true)
    public List<GitHubIssueResponse> getSavedIssues(UUID userId) {
        List<SavedIssue> saved = savedIssueRepository.findByUserIdOrderBySavedAtDesc(userId);
        List<UserSkill> skills = userSkillRepository.findByUserIdWithSkill(userId);

        return saved.stream()
                .map(s -> {
                    Double score = suitabilityService.evaluateSuitability(s.getIssue(), skills).suitabilityScore();
                    return GitHubIssueResponse.from(s.getIssue(), true, score);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public IssueSuitabilityResponse getIssueSuitability(UUID userId, UUID issueId) {
        GitHubIssue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));
        List<UserSkill> skills = userSkillRepository.findByUserIdWithSkill(userId);
        return suitabilityService.evaluateSuitability(issue, skills);
    }

    @Transactional(readOnly = true)
    public GitHubAiAnalysisResponse analyzeRepository(UUID repositoryId) {
        GitHubRepository repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found: " + repositoryId));
        return aiIntelligenceService.analyzeRepository(repo);
    }

    @Transactional(readOnly = true)
    public GitHubAiAnalysisResponse analyzeIssue(UUID issueId) {
        GitHubIssue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));
        return aiIntelligenceService.analyzeIssue(issue);
    }

    // Contribution Workspaces
    @Transactional(readOnly = true)
    public List<ContributionWorkspaceResponse> getContributionWorkspaces(UUID userId, ContributionStatus status) {
        List<ContributionWorkspace> list;
        if (status != null) {
            list = workspaceRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(userId, status);
        } else {
            list = workspaceRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        }
        return list.stream().map(ContributionWorkspaceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ContributionWorkspaceResponse getContributionWorkspace(UUID userId, UUID workspaceId) {
        ContributionWorkspace w = workspaceRepository.findByIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution workspace not found: " + workspaceId));
        return ContributionWorkspaceResponse.from(w);
    }

    public ContributionWorkspaceResponse createContributionWorkspace(UUID userId, ContributionWorkspaceRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        GitHubIssue issue = null;
        GitHubRepository repository = null;

        if (request.issueId() != null) {
            issue = issueRepository.findById(request.issueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + request.issueId()));
            repository = issue.getRepository();
        } else if (request.repositoryId() != null) {
            repository = repositoryRepository.findById(request.repositoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Repository not found: " + request.repositoryId()));
        } else {
            throw new IllegalArgumentException("Either repositoryId or issueId must be provided");
        }

        RoadmapTask roadmapTask = null;
        if (request.roadmapTaskId() != null) {
            roadmapTask = roadmapTaskRepository.findById(request.roadmapTaskId()).orElse(null);
        }

        ContributionWorkspace workspace = new ContributionWorkspace(
                user,
                repository,
                issue,
                roadmapTask,
                request.title(),
                request.status() != null ? request.status() : ContributionStatus.INTERESTED
        );

        if (request.targetBranch() != null && !request.targetBranch().isBlank()) {
            workspace.setTargetBranch(request.targetBranch());
        }
        if (request.pullRequestUrl() != null) {
            workspace.setPullRequestUrl(request.pullRequestUrl());
        }
        if (request.researchNotes() != null) {
            workspace.setResearchNotes(request.researchNotes());
        }
        if (request.solutionPlan() != null) {
            workspace.setSolutionPlan(request.solutionPlan());
        }

        ContributionWorkspace saved = workspaceRepository.save(workspace);
        return ContributionWorkspaceResponse.from(saved);
    }

    public ContributionWorkspaceResponse updateContributionWorkspace(UUID userId, UUID workspaceId, ContributionWorkspaceRequest request) {
        ContributionWorkspace workspace = workspaceRepository.findByIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution workspace not found: " + workspaceId));

        if (request.title() != null && !request.title().isBlank()) {
            workspace.setTitle(request.title());
        }
        if (request.status() != null) {
            workspace.setStatus(request.status());
        }
        if (request.targetBranch() != null) {
            workspace.setTargetBranch(request.targetBranch());
        }
        if (request.pullRequestUrl() != null) {
            workspace.setPullRequestUrl(request.pullRequestUrl());
        }
        if (request.researchNotes() != null) {
            workspace.setResearchNotes(request.researchNotes());
        }
        if (request.solutionPlan() != null) {
            workspace.setSolutionPlan(request.solutionPlan());
        }
        if (request.roadmapTaskId() != null) {
            RoadmapTask task = roadmapTaskRepository.findById(request.roadmapTaskId()).orElse(null);
            workspace.setRoadmapTask(task);
        }

        ContributionWorkspace updated = workspaceRepository.save(workspace);
        return ContributionWorkspaceResponse.from(updated);
    }

    public void deleteContributionWorkspace(UUID userId, UUID workspaceId) {
        ContributionWorkspace workspace = workspaceRepository.findByIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution workspace not found: " + workspaceId));
        workspaceRepository.delete(workspace);
    }
}
