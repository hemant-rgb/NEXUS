import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import React from "react";
import { render, screen, waitFor, fireEvent, cleanup } from "@testing-library/react";
import GitHubPage from "@/app/(app)/github/page";
import { api } from "@/lib/api";
import { GitHubConnection, GitHubIssue, GitHubRepository, ContributionWorkspace } from "@/lib/types";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn() }),
  useParams: () => ({}),
  useSearchParams: () => new URLSearchParams(),
}));

vi.mock("@/lib/api", () => ({
  api: {
    github: {
      getConnection: vi.fn(),
      connect: vi.fn(),
      disconnect: vi.fn(),
      searchIssues: vi.fn(),
      searchRepositories: vi.fn(),
      getContributions: vi.fn(),
      saveIssue: vi.fn(),
      unsaveIssue: vi.fn(),
      createContribution: vi.fn(),
      updateContribution: vi.fn(),
    },
  },
}));

describe("NEXUS GitHub Intelligence UI (Phase 6)", () => {
  const mockConnection: GitHubConnection = {
    id: "conn-1",
    githubUsername: "octodev",
    avatarUrl: "https://example.com/avatar.png",
    profileUrl: "https://github.com/octodev",
    publicReposCount: 18,
    followersCount: 42,
    followingCount: 10,
    status: "CONNECTED",
    connectedAt: "2026-11-14T10:00:00Z",
  };

  const mockIssue: GitHubIssue = {
    id: "issue-1",
    githubIssueId: 35001,
    repositoryId: "repo-1",
    repositoryFullName: "spring-projects/spring-boot",
    primaryLanguage: "Java",
    issueNumber: 35001,
    title: "Support virtual threads in WebFlux dispatcher",
    body: "Details about loom integration in WebFlux",
    htmlUrl: "https://github.com/spring-projects/spring-boot/issues/35001",
    state: "OPEN",
    labels: ["good first issue", "help wanted"],
    commentsCount: 3,
    isGoodFirstIssue: true,
    isHelpWanted: true,
    isSaved: false,
    suitabilityScore: 85.0,
  };

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(api.github.getConnection).mockResolvedValue(mockConnection);
    vi.mocked(api.github.searchIssues).mockResolvedValue([mockIssue]);
    vi.mocked(api.github.searchRepositories).mockResolvedValue([]);
    vi.mocked(api.github.getContributions).mockResolvedValue([]);
  });

  afterEach(() => {
    cleanup();
  });

  it("renders connected GitHub profile details with encrypted status indicator", async () => {
    render(<GitHubPage />);

    await waitFor(() => {
      expect(screen.getByText("@octodev")).toBeDefined();
      expect(screen.getByText("Connected")).toBeDefined();
      expect(screen.getByText(/18 public repos/)).toBeDefined();
      expect(screen.getByText(/AES-256 Encrypted/)).toBeDefined();
    });

    await waitFor(() => {
      expect(api.github.searchIssues).toHaveBeenCalled();
    });
  });

  it("renders open-source issues with suitability score badge", async () => {
    render(<GitHubPage />);

    await waitFor(() => {
      expect(api.github.searchIssues).toHaveBeenCalled();
    });

    await waitFor(() => {
      expect(screen.queryByText(/Evaluating issue suitability across catalog/i)).toBeNull();
    });

    await waitFor(() => {
      expect(screen.getAllByText(/spring-projects\/spring-boot/i).length).toBeGreaterThan(0);
      expect(screen.getAllByText(/Support virtual threads in WebFlux dispatcher/i).length).toBeGreaterThan(0);
      expect(screen.getByText("85% Match")).toBeDefined();
      expect(screen.getAllByText(/good first issue/i).length).toBeGreaterThan(0);
    });
  });

  it("allows switching to Curated Repositories tab", async () => {
    const mockRepo: GitHubRepository = {
      id: "repo-1",
      githubRepoId: 101,
      ownerLogin: "spring-projects",
      name: "spring-boot",
      fullName: "spring-projects/spring-boot",
      description: "Spring Boot production application framework",
      htmlUrl: "https://github.com/spring-projects/spring-boot",
      primaryLanguage: "Java",
      starsCount: 74000,
      forksCount: 41000,
      openIssuesCount: 500,
      defaultBranch: "main",
      hasContributingGuide: true,
    };
    vi.mocked(api.github.searchRepositories).mockResolvedValue([mockRepo]);

    render(<GitHubPage />);

    const repoTab = screen.getByText("Curated Repositories");
    fireEvent.click(repoTab);

    await waitFor(() => {
      expect(api.github.searchRepositories).toHaveBeenCalled();
      expect(screen.getByText("spring-boot")).toBeDefined();
      expect(screen.getByText(/74,000/)).toBeDefined();
    });
  });
});
