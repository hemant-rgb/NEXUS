"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import {
  GitBranch,
  Star,
  GitFork,
  ExternalLink,
  ShieldCheck,
  Search,
  CheckCircle2,
  AlertCircle,
  FolderGit2,
  Bookmark,
  Sparkles,
  Plus,
  RefreshCw,
  LogOut,
  Tag,
  ArrowRight
} from "lucide-react";
import { api } from "@/lib/api";
import {
  GitHubConnection,
  GitHubRepository,
  GitHubIssue,
  ContributionWorkspace,
  ContributionStatus
} from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";

export default function GitHubPage() {
  const [connection, setConnection] = useState<GitHubConnection | null>(null);
  const [loadingConnection, setLoadingConnection] = useState(true);

  // Connect form state
  const [tokenInput, setTokenInput] = useState("");
  const [connecting, setConnecting] = useState(false);
  const [connectError, setConnectError] = useState<string | null>(null);

  // Tabs: "issues" | "repositories" | "workspaces"
  const [activeTab, setActiveTab] = useState<"issues" | "repositories" | "workspaces">("issues");

  // Repositories & issues state
  const [repositories, setRepositories] = useState<GitHubRepository[]>([]);
  const [issues, setIssues] = useState<GitHubIssue[]>([]);
  const [workspaces, setWorkspaces] = useState<ContributionWorkspace[]>([]);
  const [loadingData, setLoadingData] = useState(false);

  // Filters
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedLanguage, setSelectedLanguage] = useState<string>("ALL");
  const [goodFirstOnly, setGoodFirstOnly] = useState<boolean>(true);

  // New Workspace modal
  const [showWorkspaceModal, setShowWorkspaceModal] = useState(false);
  const [workspaceTargetIssue, setWorkspaceTargetIssue] = useState<GitHubIssue | null>(null);
  const [wsTitle, setWsTitle] = useState("");
  const [wsBranch, setWsBranch] = useState("feature/issue-fix");
  const [wsNotes, setWsNotes] = useState("");
  const [creatingWs, setCreatingWs] = useState(false);

  // Load connection
  const loadConnection = async () => {
    try {
      setLoadingConnection(true);
      const conn = await api.github.getConnection();
      setConnection(conn);
    } catch {
      setConnection(null);
    } finally {
      setLoadingConnection(false);
    }
  };

  // Load tab content
  const loadContent = async () => {
    setLoadingData(true);
    try {
      if (activeTab === "issues") {
        const data = await api.github.searchIssues({
          query: searchQuery,
          language: selectedLanguage !== "ALL" ? selectedLanguage : undefined,
          goodFirstOnly: goodFirstOnly ? true : undefined,
        });
        setIssues(Array.isArray(data) ? data : []);
      } else if (activeTab === "repositories") {
        const data = await api.github.searchRepositories(searchQuery, selectedLanguage);
        setRepositories(data);
      } else if (activeTab === "workspaces") {
        const data = await api.github.getContributions();
        setWorkspaces(data);
      }
    } catch (err) {
      console.error("Failed to load GitHub content", err);
    } finally {
      setLoadingData(false);
    }
  };

  useEffect(() => {
    loadConnection();
  }, []);

  useEffect(() => {
    loadContent();
  }, [activeTab, selectedLanguage, goodFirstOnly]);

  const handleConnect = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!tokenInput.trim()) return;
    setConnecting(true);
    setConnectError(null);
    try {
      const conn = await api.github.connect({
        accessToken: tokenInput.trim(),
        scopes: "read:user,repo",
      });
      setConnection(conn);
      setTokenInput("");
      loadContent();
    } catch (err: any) {
      setConnectError(err.message || "Failed to authenticate GitHub token");
    } finally {
      setConnecting(false);
    }
  };

  const handleDisconnect = async () => {
    if (!confirm("Are you sure you want to disconnect your GitHub account?")) return;
    try {
      await api.github.disconnect();
      setConnection(null);
    } catch (err) {
      console.error(err);
    }
  };

  const handleSaveRepo = async (repo: GitHubRepository) => {
    try {
      if (repo.isSaved) {
        await api.github.unsaveRepository(repo.id);
      } else {
        await api.github.saveRepository(repo.id);
      }
      setRepositories((prev) =>
        prev.map((r) => (r.id === repo.id ? { ...r, isSaved: !r.isSaved } : r))
      );
    } catch (err) {
      console.error(err);
    }
  };

  const handleSaveIssue = async (issue: GitHubIssue) => {
    try {
      if (issue.isSaved) {
        await api.github.unsaveIssue(issue.id);
      } else {
        await api.github.saveIssue(issue.id);
      }
      setIssues((prev) =>
        prev.map((i) => (i.id === issue.id ? { ...i, isSaved: !i.isSaved } : i))
      );
    } catch (err) {
      console.error(err);
    }
  };

  const handleOpenWorkspaceModal = (issue: GitHubIssue) => {
    setWorkspaceTargetIssue(issue);
    setWsTitle(`Contribute to #${issue.issueNumber}: ${issue.title}`);
    setWsBranch(`fix/issue-${issue.issueNumber}`);
    setWsNotes("");
    setShowWorkspaceModal(true);
  };

  const handleCreateWorkspace = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!workspaceTargetIssue) return;
    setCreatingWs(true);
    try {
      await api.github.createContribution({
        repositoryId: workspaceTargetIssue.repositoryId,
        issueId: workspaceTargetIssue.id,
        title: wsTitle,
        status: "RESEARCHING",
        targetBranch: wsBranch,
        researchNotes: wsNotes,
      });
      setShowWorkspaceModal(false);
      setActiveTab("workspaces");
    } catch (err) {
      console.error(err);
    } finally {
      setCreatingWs(false);
    }
  };

  const handleUpdateStatus = async (wsId: string, nextStatus: ContributionStatus) => {
    try {
      await api.github.updateContribution(wsId, { status: nextStatus });
      setWorkspaces((prev) =>
        prev.map((w) => (w.id === wsId ? { ...w, status: nextStatus } : w))
      );
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-surface-border pb-5">
        <div>
          <div className="flex items-center gap-2">
            <span className="font-mono text-xs uppercase tracking-widest text-brand font-semibold">
              NEXUS Open Source Intelligence
            </span>
            <Badge variant="outline" className="font-mono text-[10px] text-text-muted">
              Phase 6
            </Badge>
          </div>
          <h1 className="text-2xl font-mono font-bold text-text-primary mt-1">
            GitHub & Contribution Intelligence
          </h1>
          <p className="text-xs font-mono text-text-secondary mt-1 max-w-2xl">
            Personalized open-source issue recommendations, suitability matching against verified skills, and autonomous contribution workspaces.
          </p>
        </div>

        {connection && (
          <div className="flex items-center gap-3">
            <Button
              variant="outline"
              size="sm"
              onClick={loadContent}
              disabled={loadingData}
              className="font-mono text-xs"
            >
              <RefreshCw className={`h-3.5 w-3.5 mr-1.5 ${loadingData ? "animate-spin" : ""}`} />
              Sync
            </Button>
            <Button
              variant="ghost"
              size="sm"
              onClick={handleDisconnect}
              className="font-mono text-xs text-danger hover:text-danger hover:bg-danger/10"
            >
              <LogOut className="h-3.5 w-3.5 mr-1.5" />
              Disconnect
            </Button>
          </div>
        )}
      </div>

      {/* GitHub Account Connection Card */}
      {!loadingConnection && (
        <div className="p-4 rounded-lg border border-surface-border bg-surface-card">
          {connection ? (
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div className="flex items-center gap-3.5">
                {connection.avatarUrl ? (
                  <img
                    src={connection.avatarUrl}
                    alt={connection.githubUsername}
                    className="h-10 w-10 rounded-full border border-surface-border"
                  />
                ) : (
                  <div className="h-10 w-10 rounded-full bg-brand/20 border border-brand/40 flex items-center justify-center font-mono text-sm text-brand font-bold">
                    GH
                  </div>
                )}
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-mono font-bold text-sm text-text-primary">
                      @{connection.githubUsername}
                    </span>
                    <Badge variant="success" className="font-mono text-[10px] px-2 py-0">
                      Connected
                    </Badge>
                  </div>
                  <div className="flex items-center gap-4 text-xs font-mono text-text-muted mt-0.5">
                    <span>{connection.publicReposCount} public repos</span>
                    <span>•</span>
                    <span>{connection.followersCount} followers</span>
                    <span>•</span>
                    <span className="flex items-center gap-1 text-text-secondary">
                      <ShieldCheck className="h-3.5 w-3.5 text-brand" /> AES-256 Encrypted
                    </span>
                  </div>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <a
                  href={connection.profileUrl || `https://github.com/${connection.githubUsername}`}
                  target="_blank"
                  rel="noreferrer"
                  className="font-mono text-xs text-text-muted hover:text-brand flex items-center gap-1 px-3 py-1.5 rounded border border-surface-border bg-bg-secondary hover:bg-surface-hover transition-colors"
                >
                  View Profile <ExternalLink className="h-3 w-3" />
                </a>
              </div>
            </div>
          ) : (
            <div className="space-y-3">
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="font-mono text-sm font-semibold text-text-primary flex items-center gap-2">
                    <GitBranch className="h-4 w-4 text-brand" /> Connect GitHub Personal Access Token
                  </h3>
                  <p className="text-xs font-mono text-text-muted mt-1">
                    Connect securely to analyze your code style, calculate issue suitability scores, and sync contribution branches.
                  </p>
                </div>
                <div className="hidden sm:flex items-center gap-1 font-mono text-[10px] text-text-muted bg-surface-hover px-2 py-1 rounded border border-surface-border">
                  <ShieldCheck className="h-3.5 w-3.5 text-brand" />
                  AES-256-GCM Encrypted at Rest
                </div>
              </div>

              <form onSubmit={handleConnect} className="flex flex-col sm:flex-row gap-2.5 pt-2">
                <div className="flex-1">
                  <Input
                    type="password"
                    placeholder="Enter GitHub Personal Access Token (classic or fine-grained)"
                    value={tokenInput}
                    onChange={(e) => setTokenInput(e.target.value)}
                    className="font-mono text-xs bg-bg-main"
                  />
                </div>
                <Button type="submit" disabled={connecting || !tokenInput.trim()} className="font-mono text-xs">
                  {connecting ? "Connecting..." : "Connect GitHub"}
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => {
                    setTokenInput("ghp_nexus_mock_sandbox_token_offline_2026");
                  }}
                  className="font-mono text-xs text-text-muted"
                >
                  Use Demo Token
                </Button>
              </form>

              {connectError && (
                <p className="text-xs font-mono text-danger flex items-center gap-1">
                  <AlertCircle className="h-3.5 w-3.5" /> {connectError}
                </p>
              )}
            </div>
          )}
        </div>
      )}

      {/* Tabs */}
      <div className="flex items-center gap-2 border-b border-surface-border">
        <button
          onClick={() => setActiveTab("issues")}
          className={`px-4 py-2 font-mono text-xs border-b-2 transition-colors ${
            activeTab === "issues"
              ? "border-brand text-brand font-semibold"
              : "border-transparent text-text-secondary hover:text-text-primary"
          }`}
        >
          Issue Discovery & Match
        </button>
        <button
          onClick={() => setActiveTab("repositories")}
          className={`px-4 py-2 font-mono text-xs border-b-2 transition-colors ${
            activeTab === "repositories"
              ? "border-brand text-brand font-semibold"
              : "border-transparent text-text-secondary hover:text-text-primary"
          }`}
        >
          Curated Repositories
        </button>
        <button
          onClick={() => setActiveTab("workspaces")}
          className={`px-4 py-2 font-mono text-xs border-b-2 transition-colors ${
            activeTab === "workspaces"
              ? "border-brand text-brand font-semibold"
              : "border-transparent text-text-secondary hover:text-text-primary"
          }`}
        >
          Contribution Workspaces {workspaces.length > 0 && `(${workspaces.length})`}
        </button>
      </div>

      {/* Filter controls */}
      {activeTab !== "workspaces" && (
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 p-3 rounded-lg border border-surface-border bg-bg-secondary">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-2.5 h-3.5 w-3.5 text-text-muted" />
            <input
              type="text"
              placeholder={activeTab === "issues" ? "Filter issues by title or label..." : "Search repositories..."}
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && loadContent()}
              className="w-full pl-9 pr-3 py-1.5 bg-bg-main border border-surface-border rounded text-xs font-mono text-text-primary placeholder:text-text-muted focus:outline-none focus:border-brand"
            />
          </div>

          <div className="flex items-center gap-2">
            <select
              value={selectedLanguage}
              onChange={(e) => setSelectedLanguage(e.target.value)}
              className="px-2.5 py-1.5 bg-bg-main border border-surface-border rounded text-xs font-mono text-text-secondary focus:outline-none focus:border-brand"
            >
              <option value="ALL">All Languages</option>
              <option value="Java">Java</option>
              <option value="TypeScript">TypeScript / JavaScript</option>
              <option value="Go">Go</option>
              <option value="Python">Python</option>
              <option value="Rust">Rust</option>
            </select>

            {activeTab === "issues" && (
              <label className="flex items-center gap-1.5 text-xs font-mono text-text-secondary cursor-pointer select-none px-2 py-1 rounded bg-bg-main border border-surface-border">
                <input
                  type="checkbox"
                  checked={goodFirstOnly}
                  onChange={(e) => setGoodFirstOnly(e.target.checked)}
                  className="rounded border-surface-border text-brand focus:ring-0"
                />
                Good First Issue Only
              </label>
            )}

            <Button size="sm" onClick={loadContent} className="font-mono text-xs">
              Search
            </Button>
          </div>
        </div>
      )}

      {/* Tab Content: ISSUES */}
      {activeTab === "issues" && (
        <div className="space-y-3">
          {loadingData ? (
            <div className="p-12 text-center text-xs font-mono text-text-muted">
              Evaluating issue suitability across catalog...
            </div>
          ) : issues.length === 0 ? (
            <div className="p-12 rounded-lg border border-surface-border bg-surface-card text-center space-y-2">
              <p className="font-mono text-xs text-text-muted">No open-source issues matched your filters.</p>
              <Button size="sm" variant="outline" onClick={() => { setSearchQuery(""); setSelectedLanguage("ALL"); setGoodFirstOnly(false); }} className="font-mono text-xs">
                Reset Filters
              </Button>
            </div>
          ) : (
            issues.map((issue) => {
              const score = issue.suitabilityScore != null ? Math.round(issue.suitabilityScore) : null;
              return (
                <div
                  key={issue.id}
                  className="p-4 rounded-lg border border-surface-border bg-surface-card hover:border-surface-hover transition-colors space-y-3"
                >
                  <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-2">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="font-mono text-xs text-brand font-semibold">
                          {issue.repositoryFullName}
                        </span>
                        <span className="text-text-muted text-xs font-mono">#{issue.issueNumber}</span>
                        {issue.primaryLanguage && (
                          <Badge variant="outline" className="font-mono text-[10px]">
                            {issue.primaryLanguage}
                          </Badge>
                        )}
                        {issue.isGoodFirstIssue && (
                          <Badge variant="success" className="font-mono text-[10px]">
                            good first issue
                          </Badge>
                        )}
                        {issue.isHelpWanted && (
                          <Badge variant="secondary" className="font-mono text-[10px]">
                            help wanted
                          </Badge>
                        )}
                      </div>

                      <h3 className="font-mono text-sm font-semibold text-text-primary">
                        <a
                          href={issue.htmlUrl}
                          target="_blank"
                          rel="noreferrer"
                          className="hover:text-brand flex items-center gap-1.5"
                        >
                          {issue.title}
                          <ExternalLink className="h-3 w-3 inline text-text-muted" />
                        </a>
                      </h3>
                    </div>

                    {/* Suitability score badge */}
                    {score != null && (
                      <div className="flex items-center gap-2 shrink-0">
                        <div
                          className={`px-3 py-1 rounded border font-mono text-xs flex items-center gap-1.5 ${
                            score >= 75
                              ? "bg-brand/10 border-brand/40 text-brand"
                              : score >= 50
                              ? "bg-warning/10 border-warning/40 text-warning"
                              : "bg-surface-hover border-surface-border text-text-secondary"
                          }`}
                        >
                          <Sparkles className="h-3.5 w-3.5" />
                          <span>{score}% Match</span>
                        </div>
                      </div>
                    )}
                  </div>

                  {issue.body && (
                    <p className="font-mono text-xs text-text-secondary line-clamp-2">
                      {issue.body}
                    </p>
                  )}

                  <div className="flex flex-wrap items-center justify-between gap-3 pt-2 border-t border-surface-border/60">
                    <div className="flex items-center gap-1.5 flex-wrap">
                      <Tag className="h-3 w-3 text-text-muted" />
                      {issue.labels.slice(0, 4).map((label) => (
                        <span
                          key={label}
                          className="text-[10px] font-mono px-2 py-0.5 rounded bg-bg-main border border-surface-border text-text-muted"
                        >
                          {label}
                        </span>
                      ))}
                    </div>

                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => handleSaveIssue(issue)}
                        className={`p-1.5 rounded border text-xs font-mono transition-colors ${
                          issue.isSaved
                            ? "bg-brand/20 border-brand text-brand"
                            : "border-surface-border text-text-muted hover:text-text-primary"
                        }`}
                        title={issue.isSaved ? "Saved" : "Save issue"}
                      >
                        <Bookmark className="h-3.5 w-3.5" />
                      </button>

                      <Button
                        size="sm"
                        onClick={() => handleOpenWorkspaceModal(issue)}
                        className="font-mono text-xs"
                      >
                        <Plus className="h-3.5 w-3.5 mr-1" />
                        Start Workspace
                      </Button>
                    </div>
                  </div>
                </div>
              );
            })
          )}
        </div>
      )}

      {/* Tab Content: REPOSITORIES */}
      {activeTab === "repositories" && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {loadingData ? (
            <div className="col-span-2 p-12 text-center text-xs font-mono text-text-muted">
              Loading open-source repositories...
            </div>
          ) : repositories.length === 0 ? (
            <div className="col-span-2 p-12 rounded-lg border border-surface-border bg-surface-card text-center text-xs font-mono text-text-muted">
              No repositories matched your query.
            </div>
          ) : (
            repositories.map((repo) => (
              <div
                key={repo.id}
                className="p-4 rounded-lg border border-surface-border bg-surface-card hover:border-surface-hover transition-colors flex flex-col justify-between space-y-3"
              >
                <div className="space-y-2">
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <span className="text-[10px] font-mono uppercase text-brand font-semibold">
                        {repo.ownerLogin}
                      </span>
                      <h3 className="font-mono text-sm font-bold text-text-primary">
                        <a
                          href={repo.htmlUrl}
                          target="_blank"
                          rel="noreferrer"
                          className="hover:text-brand flex items-center gap-1"
                        >
                          {repo.name}
                          <ExternalLink className="h-3 w-3 inline text-text-muted" />
                        </a>
                      </h3>
                    </div>

                    <button
                      onClick={() => handleSaveRepo(repo)}
                      className={`p-1.5 rounded border text-xs font-mono transition-colors ${
                        repo.isSaved
                          ? "bg-brand/20 border-brand text-brand"
                          : "border-surface-border text-text-muted hover:text-text-primary"
                      }`}
                      title={repo.isSaved ? "Saved" : "Save repo"}
                    >
                      <Bookmark className="h-3.5 w-3.5" />
                    </button>
                  </div>

                  {repo.description && (
                    <p className="font-mono text-xs text-text-secondary line-clamp-2">
                      {repo.description}
                    </p>
                  )}
                </div>

                <div className="pt-2 border-t border-surface-border/60 space-y-2">
                  <div className="flex items-center gap-4 text-xs font-mono text-text-muted">
                    {repo.primaryLanguage && (
                      <span className="flex items-center gap-1 text-text-primary">
                        <span className="h-2 w-2 rounded-full bg-brand" />
                        {repo.primaryLanguage}
                      </span>
                    )}
                    <span className="flex items-center gap-1">
                      <Star className="h-3 w-3" /> {repo.starsCount?.toLocaleString()}
                    </span>
                    <span className="flex items-center gap-1">
                      <GitFork className="h-3 w-3" /> {repo.forksCount?.toLocaleString()}
                    </span>
                  </div>

                  <div className="flex items-center justify-between pt-1">
                    <span className="text-[10px] font-mono text-text-muted">
                      Branch: {repo.defaultBranch}
                    </span>
                    {repo.contributingUrl && (
                      <a
                        href={repo.contributingUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="font-mono text-[10px] text-brand hover:underline"
                      >
                        CONTRIBUTING.md
                      </a>
                    )}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* Tab Content: WORKSPACES */}
      {activeTab === "workspaces" && (
        <div className="space-y-4">
          {loadingData ? (
            <div className="p-12 text-center text-xs font-mono text-text-muted">
              Loading active contribution workspaces...
            </div>
          ) : workspaces.length === 0 ? (
            <div className="p-12 rounded-lg border border-surface-border bg-surface-card text-center space-y-3">
              <FolderGit2 className="h-8 w-8 text-text-muted mx-auto" />
              <p className="font-mono text-xs text-text-muted">
                No active contribution workspaces yet. Discover an issue and click &quot;Start Workspace&quot;.
              </p>
              <Button size="sm" onClick={() => setActiveTab("issues")} className="font-mono text-xs">
                Browse Open Issues
              </Button>
            </div>
          ) : (
            workspaces.map((ws) => (
              <div
                key={ws.id}
                className="p-4 rounded-lg border border-surface-border bg-surface-card space-y-3"
              >
                <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-3">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <Badge variant="outline" className="font-mono text-[10px] text-brand">
                        {ws.repositoryFullName || "Repository"}
                      </Badge>
                      <span className="font-mono text-xs text-text-muted">Branch: `{ws.targetBranch}`</span>
                    </div>
                    <h3 className="font-mono text-sm font-bold text-text-primary">{ws.title}</h3>
                  </div>

                  {/* Status Dropdown */}
                  <div className="flex items-center gap-2">
                    <select
                      value={ws.status}
                      onChange={(e) => handleUpdateStatus(ws.id, e.target.value as ContributionStatus)}
                      className="px-2.5 py-1 bg-bg-main border border-surface-border rounded text-xs font-mono text-text-primary focus:outline-none focus:border-brand"
                    >
                      <option value="INTERESTED">Interested</option>
                      <option value="RESEARCHING">Researching</option>
                      <option value="PLANNED">Planned</option>
                      <option value="IN_PROGRESS">In Progress</option>
                      <option value="REVIEW_REQUESTED">Review Requested</option>
                      <option value="CHANGES_REQUESTED">Changes Requested</option>
                      <option value="MERGED">Merged 🎉</option>
                      <option value="CLOSED">Closed</option>
                      <option value="ABANDONED">Abandoned</option>
                    </select>
                  </div>
                </div>

                {ws.researchNotes && (
                  <div className="p-2.5 rounded bg-bg-main border border-surface-border text-xs font-mono text-text-secondary">
                    <span className="text-text-muted font-semibold block mb-0.5">Research & Solution Notes:</span>
                    {ws.researchNotes}
                  </div>
                )}

                <div className="flex items-center justify-between text-xs font-mono text-text-muted pt-2 border-t border-surface-border/60">
                  <span>Updated {new Date(ws.updatedAt).toLocaleDateString()}</span>
                  {ws.pullRequestUrl ? (
                    <a
                      href={ws.pullRequestUrl}
                      target="_blank"
                      rel="noreferrer"
                      className="text-brand hover:underline flex items-center gap-1"
                    >
                      Pull Request <ExternalLink className="h-3 w-3" />
                    </a>
                  ) : (
                    <span className="text-text-muted italic">No PR linked yet</span>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* Start Contribution Workspace Modal */}
      {showWorkspaceModal && workspaceTargetIssue && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="w-full max-w-lg p-5 rounded-lg border border-surface-border bg-surface-card space-y-4">
            <div className="border-b border-surface-border pb-3">
              <h3 className="font-mono text-sm font-bold text-text-primary flex items-center gap-2">
                <Plus className="h-4 w-4 text-brand" /> Start Contribution Workspace
              </h3>
              <p className="text-xs font-mono text-text-muted mt-1">
                {workspaceTargetIssue.repositoryFullName} • Issue #{workspaceTargetIssue.issueNumber}
              </p>
            </div>

            <form onSubmit={handleCreateWorkspace} className="space-y-3 font-mono text-xs">
              <div>
                <label className="block text-text-muted mb-1">Workspace Title</label>
                <Input
                  value={wsTitle}
                  onChange={(e) => setWsTitle(e.target.value)}
                  className="font-mono text-xs bg-bg-main"
                  required
                />
              </div>

              <div>
                <label className="block text-text-muted mb-1">Local / Feature Branch</label>
                <Input
                  value={wsBranch}
                  onChange={(e) => setWsBranch(e.target.value)}
                  className="font-mono text-xs bg-bg-main"
                  required
                />
              </div>

              <div>
                <label className="block text-text-muted mb-1">Initial Research Notes (Optional)</label>
                <textarea
                  rows={3}
                  value={wsNotes}
                  onChange={(e) => setWsNotes(e.target.value)}
                  placeholder="Record affected modules, reproduction steps, or plan..."
                  className="w-full p-2 bg-bg-main border border-surface-border rounded text-xs font-mono text-text-primary focus:outline-none focus:border-brand"
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-2 border-t border-surface-border">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setShowWorkspaceModal(false)}
                  className="font-mono text-xs"
                >
                  Cancel
                </Button>
                <Button type="submit" disabled={creatingWs} className="font-mono text-xs">
                  {creatingWs ? "Creating..." : "Launch Workspace"}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
