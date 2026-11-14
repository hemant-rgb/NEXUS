"use client";

import React, { useEffect, useState } from "react";
import {
  Bot,
  Play,
  CheckCircle2,
  XCircle,
  Clock,
  ShieldCheck,
  AlertOctagon,
  Sparkles,
  Terminal,
  ChevronRight,
  ArrowRight,
  RotateCcw,
  Compass,
  Search,
  BookOpen,
  Map,
  GitPullRequest,
  FileText,
  Activity,
  DollarSign,
  Cpu,
} from "lucide-react";
import { api } from "@/lib/api";
import { AgentMetadata, AgentRun, AgentApproval, AgentStep } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";

export default function AgentsWorkspacePage() {
  const [activeTab, setActiveTab] = useState<"workspace" | "runs" | "approvals">("workspace");
  const [agents, setAgents] = useState<AgentMetadata[]>([]);
  const [runs, setRuns] = useState<AgentRun[]>([]);
  const [approvals, setApprovals] = useState<AgentApproval[]>([]);
  const [selectedAgent, setSelectedAgent] = useState<AgentMetadata | null>(null);
  const [activeRun, setActiveRun] = useState<AgentRun | null>(null);

  // Form inputs
  const [goal, setGoal] = useState<string>("");
  const [opportunityId, setOpportunityId] = useState<string>("");
  const [applicationId, setApplicationId] = useState<string>("");
  const [requestExternalSubmission, setRequestExternalSubmission] = useState<boolean>(false);
  const [launching, setLaunching] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [message, setMessage] = useState<string | null>(null);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [agentsRes, runsRes, approvalsRes] = await Promise.all([
        api.agents.list(),
        api.agents.getUserRuns(0, 15),
        api.agents.getApprovals(),
      ]);
      setAgents(agentsRes || []);
      setRuns(runsRes?.content || []);
      setApprovals(approvalsRes || []);

      if (agentsRes && agentsRes.length > 0 && !selectedAgent) {
        setSelectedAgent(agentsRes[0]);
      }
      if (runsRes?.content && runsRes.content.length > 0 && !activeRun) {
        setActiveRun(runsRes.content[0]);
      }
    } catch (err) {
      console.error("Failed to load agent data", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleLaunchAgent = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedAgent || !goal.trim()) return;

    setLaunching(true);
    try {
      const params: Record<string, unknown> = {};
      if (opportunityId.trim()) params.opportunityId = opportunityId.trim();
      if (applicationId.trim()) params.applicationId = applicationId.trim();
      if (requestExternalSubmission) params.requestExternalSubmission = "true";

      const run = await api.agents.startRun(selectedAgent.agentId, {
        goal: goal.trim(),
        parameters: params,
      });

      setActiveRun(run);
      setMessage(`Agent ${selectedAgent.name} deployed successfully (Run ID: ${run.id.slice(0, 8)}).`);
      await fetchData();
      setGoal("");
    } catch (err: unknown) {
      console.error("Launch error", err);
      setMessage("Failed to deploy agent run");
    } finally {
      setLaunching(false);
    }
  };

  const handleApprove = async (approvalId: string) => {
    try {
      await api.agents.approve(approvalId, "Approved in Agent Workspace");
      setMessage("Autonomous action authorized and resumed.");
      await fetchData();
    } catch (err) {
      console.error(err);
    }
  };

  const handleReject = async (approvalId: string) => {
    try {
      await api.agents.reject(approvalId, "Rejected by user");
      setMessage("Autonomous action rejected.");
      await fetchData();
    } catch (err) {
      console.error(err);
    }
  };

  const getAgentIcon = (id: string) => {
    switch (id) {
      case "SCOUT":
        return <Compass className="h-5 w-5 text-brand" />;
      case "RESEARCH":
        return <Search className="h-5 w-5 text-blue-400" />;
      case "STRATEGY":
        return <Map className="h-5 w-5 text-amber-400" />;
      case "GITHUB":
        return <GitPullRequest className="h-5 w-5 text-purple-400" />;
      case "APPLICATION":
        return <FileText className="h-5 w-5 text-emerald-400" />;
      case "MONITORING":
        return <Activity className="h-5 w-5 text-rose-400" />;
      default:
        return <Bot className="h-5 w-5 text-text-muted" />;
    }
  };

  const getAutonomyBadge = (level: number) => {
    switch (level) {
      case 0:
        return <Badge variant="neutral" className="text-[10px]">L0: Read-Only</Badge>;
      case 1:
        return <Badge variant="brand" className="text-[10px]">L1: Drafting</Badge>;
      case 2:
        return <Badge variant="warning" className="text-[10px]">L2: Internal Action</Badge>;
      case 3:
      default:
        return <Badge variant="danger" className="text-[10px]">L3: Approval Required</Badge>;
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "COMPLETED":
        return <Badge variant="success" className="text-[10px]">COMPLETED</Badge>;
      case "WAITING_APPROVAL":
        return <Badge variant="danger" className="text-[10px] animate-pulse">APPROVAL REQUIRED</Badge>;
      case "RUNNING":
        return <Badge variant="brand" className="text-[10px] animate-pulse">RUNNING</Badge>;
      case "FAILED":
        return <Badge variant="danger" className="text-[10px]">FAILED</Badge>;
      case "CANCELLED":
        return <Badge variant="neutral" className="text-[10px]">CANCELLED</Badge>;
      default:
        return <Badge variant="neutral" className="text-[10px]">{status}</Badge>;
    }
  };

  const getStepTypeBadge = (type: string) => {
    switch (type) {
      case "THINKING":
        return <span className="text-amber-400 font-mono text-[10px]">THINKING</span>;
      case "TOOL_CALL":
        return <span className="text-brand font-mono text-[10px]">TOOL CALL</span>;
      case "DECISION":
        return <span className="text-emerald-400 font-mono text-[10px]">DECISION</span>;
      case "DRAFTING":
        return <span className="text-blue-400 font-mono text-[10px]">DRAFTING</span>;
      default:
        return <span className="text-text-muted font-mono text-[10px]">{type}</span>;
    }
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-surface-border pb-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-mono font-bold text-text-primary tracking-wide">
              NEXUS Autonomous Agents
            </h1>
            <span className="h-2 w-2 rounded-full bg-brand animate-ping" />
            <Badge variant="brand" className="text-xs">
              Phase 9 Engine
            </Badge>
          </div>
          <p className="text-xs font-mono text-text-secondary mt-1">
            Specialized autonomous AI agents, controlled tool registries, multi-step execution traces, and Level 3 safety approval gating
          </p>
        </div>

        {approvals.filter((a) => a.status === "PENDING").length > 0 && (
          <div className="flex items-center gap-2 p-2 px-3 bg-danger/10 border border-danger/30 rounded-md text-xs font-mono text-danger">
            <AlertOctagon className="h-4 w-4 shrink-0" />
            <span>
              {approvals.filter((a) => a.status === "PENDING").length} Action(s) awaiting approval
            </span>
          </div>
        )}
      </div>

      {message && (
        <div className="p-3 bg-brand/10 border border-brand/30 rounded-md text-xs font-mono text-brand flex items-center justify-between">
          <span>{message}</span>
          <button onClick={() => setMessage(null)} className="hover:text-white">✕</button>
        </div>
      )}

      {/* Tabs */}
      <div className="flex items-center gap-2 border-b border-surface-border pb-2 text-xs font-mono">
        <button
          onClick={() => setActiveTab("workspace")}
          className={`px-3 py-1.5 rounded-md flex items-center gap-2 transition-colors ${
            activeTab === "workspace"
              ? "bg-surface-hover text-brand border border-surface-border font-medium"
              : "text-text-secondary hover:text-text-primary hover:bg-surface-hover"
          }`}
        >
          <Bot className="h-3.5 w-3.5" />
          <span>Agent Workspace & Execution</span>
        </button>

        <button
          onClick={() => setActiveTab("approvals")}
          className={`px-3 py-1.5 rounded-md flex items-center gap-2 transition-colors ${
            activeTab === "approvals"
              ? "bg-surface-hover text-brand border border-surface-border font-medium"
              : "text-text-secondary hover:text-text-primary hover:bg-surface-hover"
          }`}
        >
          <ShieldCheck className="h-3.5 w-3.5" />
          <span>Action Approvals ({approvals.filter((a) => a.status === "PENDING").length})</span>
        </button>

        <button
          onClick={() => setActiveTab("runs")}
          className={`px-3 py-1.5 rounded-md flex items-center gap-2 transition-colors ${
            activeTab === "runs"
              ? "bg-surface-hover text-brand border border-surface-border font-medium"
              : "text-text-secondary hover:text-text-primary hover:bg-surface-hover"
          }`}
        >
          <Clock className="h-3.5 w-3.5" />
          <span>Execution History ({runs.length})</span>
        </button>
      </div>

      {/* TAB 1: WORKSPACE */}
      {activeTab === "workspace" && (
        <div className="space-y-6">
          {/* 1. Agent Roster */}
          <div>
            <div className="text-xs font-mono uppercase tracking-widest text-text-muted mb-3 font-semibold">
              Specialized Agent Registry
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
              {agents.map((agent) => {
                const isSelected = selectedAgent?.agentId === agent.agentId;
                return (
                  <div
                    key={agent.agentId}
                    onClick={() => setSelectedAgent(agent)}
                    className={`p-4 rounded-lg border cursor-pointer transition-all ${
                      isSelected
                        ? "bg-surface-card border-brand shadow-md"
                        : "bg-surface-card/40 border-surface-border hover:border-surface-border/80 hover:bg-surface-card"
                    }`}
                  >
                    <div className="flex items-center justify-between gap-2 mb-2">
                      <div className="flex items-center gap-2.5">
                        <div className="p-2 rounded-md bg-surface-hover border border-surface-border">
                          {getAgentIcon(agent.agentId)}
                        </div>
                        <span className="text-xs font-mono font-bold text-text-primary">
                          {agent.name}
                        </span>
                      </div>
                      {getAutonomyBadge(agent.autonomyLevel)}
                    </div>
                    <p className="text-[11px] font-mono text-text-secondary line-clamp-2 mb-3">
                      {agent.description}
                    </p>
                    <div className="flex flex-wrap gap-1">
                      {agent.permittedTools.map((t) => (
                        <span
                          key={t}
                          className="px-1.5 py-0.5 rounded-sm bg-bg-main border border-surface-border text-[9px] font-mono text-text-muted"
                        >
                          {t}
                        </span>
                      ))}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* 2. Interactive Deployer & Execution Trace Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
            {/* Deployer Panel */}
            <div className="lg:col-span-5 bg-surface-card border border-surface-border rounded-lg p-5 space-y-4">
              <div className="flex items-center gap-2 border-b border-surface-border pb-3">
                <Play className="h-4 w-4 text-brand" />
                <h2 className="text-xs font-mono font-bold text-text-primary uppercase tracking-wider">
                  Deploy {selectedAgent?.name || "Agent"}
                </h2>
              </div>

              <form onSubmit={handleLaunchAgent} className="space-y-4 text-xs font-mono">
                <div>
                  <label className="text-text-muted block mb-1">Agent Goal / Objective</label>
                  <textarea
                    rows={3}
                    required
                    value={goal}
                    onChange={(e) => setGoal(e.target.value)}
                    placeholder={`e.g. ${
                      selectedAgent?.agentId === "SCOUT"
                        ? "Find 5 high-match software internships in North America"
                        : selectedAgent?.agentId === "RESEARCH"
                        ? "Investigate requirements and company provenance"
                        : selectedAgent?.agentId === "STRATEGY"
                        ? "Synthesize optimal 4-milestone roadmap trajectory"
                        : selectedAgent?.agentId === "GITHUB"
                        ? "Analyze good-first-issues and prepare PR implementation guide"
                        : selectedAgent?.agentId === "APPLICATION"
                        ? "Draft opportunity-tailored cover letter and proposal"
                        : "Audit all active milestones and trigger priority alerts"
                    }`}
                    className="w-full bg-bg-main border border-surface-border rounded p-2.5 text-text-primary placeholder:text-text-muted/60 focus:border-brand outline-none"
                  />
                </div>

                {/* Context parameters */}
                <div className="space-y-3 pt-2 border-t border-surface-border">
                  <div className="text-[11px] text-text-muted font-semibold uppercase tracking-wider">
                    Target Context (Optional)
                  </div>

                  <div>
                    <label className="text-text-muted text-[10px] block mb-1">Opportunity ID (UUID)</label>
                    <input
                      type="text"
                      value={opportunityId}
                      onChange={(e) => setOpportunityId(e.target.value)}
                      placeholder="e.g. 550e8400-e29b-41d4-a716-446655440000"
                      className="w-full bg-bg-main border border-surface-border rounded px-2.5 py-1.5 text-xs text-text-primary font-mono"
                    />
                  </div>

                  {selectedAgent?.agentId === "APPLICATION" && (
                    <>
                      <div>
                        <label className="text-text-muted text-[10px] block mb-1">Application ID (UUID)</label>
                        <input
                          type="text"
                          value={applicationId}
                          onChange={(e) => setApplicationId(e.target.value)}
                          placeholder="Application ID to attach drafts"
                          className="w-full bg-bg-main border border-surface-border rounded px-2.5 py-1.5 text-xs text-text-primary font-mono"
                        />
                      </div>

                      <label className="flex items-center gap-2 cursor-pointer pt-1">
                        <input
                          type="checkbox"
                          checked={requestExternalSubmission}
                          onChange={(e) => setRequestExternalSubmission(e.target.checked)}
                          className="rounded border-surface-border bg-bg-main text-brand"
                        />
                        <span className="text-[11px] text-amber-400">
                          Request Level 3 External Submission (Requires Safety Approval)
                        </span>
                      </label>
                    </>
                  )}
                </div>

                <Button
                  type="submit"
                  disabled={launching || !goal.trim()}
                  className="w-full flex items-center justify-center gap-2 py-2"
                >
                  <Play className={`h-3.5 w-3.5 ${launching ? "animate-spin" : ""}`} />
                  <span>{launching ? "Deploying Agent..." : `Deploy ${selectedAgent?.agentId || "Agent"}`}</span>
                </Button>
              </form>
            </div>

            {/* Execution Trace Panel */}
            <div className="lg:col-span-7 bg-surface-card border border-surface-border rounded-lg p-5 space-y-4">
              <div className="flex items-center justify-between border-b border-surface-border pb-3">
                <div className="flex items-center gap-2">
                  <Terminal className="h-4 w-4 text-brand" />
                  <h2 className="text-xs font-mono font-bold text-text-primary uppercase tracking-wider">
                    Execution Trace & Steps
                  </h2>
                </div>
                {activeRun && getStatusBadge(activeRun.status)}
              </div>

              {!activeRun ? (
                <div className="p-8 text-center text-xs font-mono text-text-muted">
                  No active agent run selected. Deploy an agent to view the real-time execution trace.
                </div>
              ) : (
                <div className="space-y-4 text-xs font-mono">
                  {/* Telemetry bar */}
                  <div className="grid grid-cols-3 gap-2 p-2.5 bg-bg-main border border-surface-border rounded text-[11px]">
                    <div>
                      <span className="text-text-muted block text-[10px]">Agent</span>
                      <span className="font-bold text-text-primary">{activeRun.agentId}</span>
                    </div>
                    <div>
                      <span className="text-text-muted block text-[10px]">Tokens Used</span>
                      <span className="font-bold text-brand">{activeRun.tokenUsage}</span>
                    </div>
                    <div>
                      <span className="text-text-muted block text-[10px]">Estimated Cost</span>
                      <span className="font-bold text-text-primary">${Number(activeRun.estimatedCost).toFixed(4)}</span>
                    </div>
                  </div>

                  {/* Summary */}
                  {activeRun.outputSummary && (
                    <div className="p-3 bg-surface-hover/60 border border-surface-border rounded text-[11px] text-text-primary leading-relaxed">
                      <span className="text-brand font-bold block mb-1">Executive Summary:</span>
                      {activeRun.outputSummary}
                    </div>
                  )}

                  {/* Step Sequence */}
                  <div className="space-y-2">
                    <div className="text-[10px] text-text-muted uppercase font-semibold">
                      Executed Steps ({activeRun.steps?.length || 0})
                    </div>
                    <div className="space-y-2 max-h-[360px] overflow-y-auto pr-1">
                      {activeRun.steps?.map((step) => (
                        <div
                          key={step.id || step.sequenceOrder}
                          className="p-3 bg-bg-main border border-surface-border rounded space-y-1.5"
                        >
                          <div className="flex items-center justify-between gap-2">
                            <div className="flex items-center gap-2">
                              <span className="h-4 w-4 rounded-full bg-surface-hover text-brand text-[9px] flex items-center justify-center font-bold">
                                {step.sequenceOrder}
                              </span>
                              {getStepTypeBadge(step.stepType)}
                              {step.toolId && (
                                <span className="px-1.5 py-0.2 rounded bg-surface-border text-[9px] text-text-muted">
                                  {step.toolId}
                                </span>
                              )}
                            </div>
                            <span className="text-[9px] text-text-muted">
                              {new Date(step.startedAt).toLocaleTimeString()}
                            </span>
                          </div>
                          {step.inputSummary && (
                            <p className="text-[10px] text-text-muted font-mono">
                              In: {step.inputSummary}
                            </p>
                          )}
                          <p className="text-[11px] text-text-secondary font-mono leading-relaxed">
                            {step.outputSummary}
                          </p>
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* Pending Approval within Trace */}
                  {activeRun.status === "WAITING_APPROVAL" && activeRun.approvals?.length > 0 && (
                    <div className="p-4 bg-danger/10 border border-danger/40 rounded-lg space-y-3">
                      <div className="flex items-center gap-2 text-danger font-bold text-xs">
                        <AlertOctagon className="h-4 w-4" />
                        <span>Level 3 Safety Pause: Authorization Required</span>
                      </div>
                      <p className="text-[11px] text-text-primary">
                        {activeRun.approvals[0].actionSummary}
                      </p>
                      <pre className="p-2 bg-bg-main rounded text-[10px] text-text-muted font-mono overflow-x-auto">
                        {activeRun.approvals[0].actionPayload}
                      </pre>
                      {activeRun.approvals[0].status === "PENDING" && (
                        <div className="flex items-center gap-3 pt-1">
                          <Button
                            size="sm"
                            onClick={() => handleApprove(activeRun.approvals[0].id)}
                            className="bg-success text-black hover:bg-success/90"
                          >
                            Authorize Action
                          </Button>
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handleReject(activeRun.approvals[0].id)}
                            className="text-danger hover:bg-danger/10"
                          >
                            Reject & Abort
                          </Button>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* TAB 2: PENDING APPROVALS */}
      {activeTab === "approvals" && (
        <div className="space-y-4">
          <div className="p-3 bg-surface-card border border-surface-border rounded-lg text-xs font-mono text-text-secondary">
            Level 3 autonomous actions (external application submission, external email dispatch, public PR publishing) are gated by mandatory user authorization.
          </div>

          {approvals.length === 0 ? (
            <div className="p-8 text-center border border-dashed border-surface-border rounded-lg text-xs font-mono text-text-muted">
              Zero pending authorization requests. All autonomous executions are compliant.
            </div>
          ) : (
            <div className="space-y-3">
              {approvals.map((appr) => (
                <div
                  key={appr.id}
                  className="p-5 rounded-lg border bg-surface-card border-surface-border space-y-3 text-xs font-mono"
                >
                  <div className="flex items-center justify-between gap-2">
                    <div className="flex items-center gap-2">
                      <ShieldCheck className="h-4 w-4 text-brand" />
                      <span className="font-bold text-text-primary">{appr.actionType}</span>
                      <Badge variant={appr.status === "PENDING" ? "danger" : "neutral"} className="text-[10px]">
                        {appr.status}
                      </Badge>
                    </div>
                    <span className="text-[10px] text-text-muted">
                      {new Date(appr.requestedAt).toLocaleString()}
                    </span>
                  </div>

                  <p className="text-text-secondary">{appr.actionSummary}</p>

                  <div className="p-3 bg-bg-main border border-surface-border rounded text-[10px] text-text-muted overflow-x-auto">
                    <code>{appr.actionPayload}</code>
                  </div>

                  {appr.status === "PENDING" ? (
                    <div className="flex items-center gap-3 pt-2">
                      <Button
                        size="sm"
                        onClick={() => handleApprove(appr.id)}
                        className="bg-success text-black hover:bg-success/90"
                      >
                        Authorize & Execute
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => handleReject(appr.id)}
                        className="text-danger hover:bg-danger/10"
                      >
                        Reject Action
                      </Button>
                    </div>
                  ) : (
                    <div className="text-[10px] text-text-muted">
                      Decided: {appr.status} ({appr.decisionReason || "No note"})
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* TAB 3: RUN HISTORY */}
      {activeTab === "runs" && (
        <div className="space-y-3">
          {runs.length === 0 ? (
            <div className="p-8 text-center text-xs font-mono text-text-muted">
              No historical runs recorded yet.
            </div>
          ) : (
            <div className="space-y-2">
              {runs.map((r) => (
                <div
                  key={r.id}
                  onClick={() => {
                    setActiveRun(r);
                    setActiveTab("workspace");
                  }}
                  className="p-4 bg-surface-card border border-surface-border hover:border-brand/40 rounded-lg cursor-pointer transition-all flex items-center justify-between gap-4 text-xs font-mono"
                >
                  <div className="space-y-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-text-primary">{r.agentId} Agent</span>
                      {getStatusBadge(r.status)}
                      <span className="text-[10px] text-text-muted">ID: {r.id.slice(0, 8)}</span>
                    </div>
                    <p className="text-text-secondary truncate">{r.inputSummary}</p>
                    <div className="flex items-center gap-3 text-[10px] text-text-muted">
                      <span>{new Date(r.createdAt).toLocaleString()}</span>
                      <span>•</span>
                      <span>{r.tokenUsage} tokens</span>
                      <span>•</span>
                      <span>{r.steps?.length || 0} steps</span>
                    </div>
                  </div>
                  <ChevronRight className="h-4 w-4 text-text-muted shrink-0" />
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
