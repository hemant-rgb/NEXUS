"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import {
  Map,
  Plus,
  Calendar,
  Clock,
  CheckCircle2,
  AlertTriangle,
  ArrowRight,
  Sparkles,
  Layers,
  Target,
  RefreshCw,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { api } from "@/lib/api";
import { RoadmapSummary } from "@/lib/types";

export default function RoadmapsPage() {
  const [roadmaps, setRoadmaps] = useState<RoadmapSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadRoadmaps();
  }, []);

  const loadRoadmaps = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await api.roadmaps.list();
      setRoadmaps(data);
    } catch (err: any) {
      setError(err?.message || "Failed to load roadmaps");
    } finally {
      setLoading(false);
    }
  };

  const totalRoadmaps = roadmaps.length;
  const activeRoadmaps = roadmaps.filter((r) => r.status === "ACTIVE").length;
  const completedRoadmaps = roadmaps.filter((r) => r.status === "COMPLETED").length;
  const totalCompletedTasks = roadmaps.reduce((acc, r) => acc + r.completedTasks, 0);

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-surface-border/80">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <Map className="h-6 w-6 text-brand" />
            <h1 className="text-2xl font-mono font-bold text-text-primary tracking-tight">
              Personalized Preparation Roadmaps
            </h1>
          </div>
          <p className="text-xs font-mono text-text-muted">
            NEXUS Strategy: Outcome-driven learning pathways, verifiable milestone checklists, and deterministic schedules.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Button variant="outline" size="sm" onClick={loadRoadmaps} disabled={loading}>
            <RefreshCw className={`mr-1.5 h-3.5 w-3.5 ${loading ? "animate-spin" : ""}`} />
            Refresh
          </Button>
          <Link href="/roadmaps/new">
            <Button variant="primary" size="sm">
              <Plus className="mr-1.5 h-4 w-4" />
              Create Roadmap
            </Button>
          </Link>
        </div>
      </div>

      {/* Metrics Banner */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 font-mono text-xs">
        <div className="p-4 rounded-md bg-surface-card border border-surface-border space-y-1">
          <span className="text-[10px] uppercase tracking-wider text-text-muted block">Total Pathways</span>
          <span className="text-xl font-bold text-text-primary">{totalRoadmaps}</span>
        </div>
        <div className="p-4 rounded-md bg-surface-card border border-surface-border space-y-1">
          <span className="text-[10px] uppercase tracking-wider text-brand block">Active Roadmaps</span>
          <span className="text-xl font-bold text-brand">{activeRoadmaps}</span>
        </div>
        <div className="p-4 rounded-md bg-surface-card border border-surface-border space-y-1">
          <span className="text-[10px] uppercase tracking-wider text-cyan-400 block">Completed Pathways</span>
          <span className="text-xl font-bold text-cyan-400">{completedRoadmaps}</span>
        </div>
        <div className="p-4 rounded-md bg-surface-card border border-surface-border space-y-1">
          <span className="text-[10px] uppercase tracking-wider text-emerald-400 block">Tasks Completed</span>
          <span className="text-xl font-bold text-emerald-400">{totalCompletedTasks}</span>
        </div>
      </div>

      {/* Loading state */}
      {loading && (
        <div className="flex h-64 items-center justify-center font-mono text-xs text-text-muted">
          Loading preparation roadmaps...
        </div>
      )}

      {/* Error state */}
      {!loading && error && (
        <div className="p-4 rounded-md bg-danger/10 border border-danger/30 text-xs font-mono text-danger flex items-center gap-2">
          <AlertTriangle className="h-4 w-4" />
          <span>{error}</span>
        </div>
      )}

      {/* Empty State */}
      {!loading && !error && roadmaps.length === 0 && (
        <div className="p-12 text-center rounded-md bg-surface-card border border-dashed border-surface-border space-y-4">
          <div className="inline-flex p-3 rounded-full bg-brand/10 text-brand mb-1">
            <Target className="h-8 w-8" />
          </div>
          <h2 className="text-base font-mono font-bold text-text-primary">No Preparation Roadmaps Found</h2>
          <p className="text-xs font-sans text-text-secondary max-w-md mx-auto">
            Bridge the gap between discovering an opportunity and becoming genuinely prepared. Generate a tailored pathway for a specific opportunity or set a career mastery goal.
          </p>
          <div className="flex flex-wrap items-center justify-center gap-3 pt-2">
            <Link href="/roadmaps/new?type=opportunity">
              <Button variant="primary" size="sm">
                <Sparkles className="mr-1.5 h-3.5 w-3.5" />
                Prepare for an Opportunity
              </Button>
            </Link>
            <Link href="/roadmaps/new?type=goal">
              <Button variant="outline" size="sm">
                <Target className="mr-1.5 h-3.5 w-3.5" />
                Set a Career Goal
              </Button>
            </Link>
          </div>
        </div>
      )}

      {/* Roadmaps Grid */}
      {!loading && !error && roadmaps.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {roadmaps.map((r) => {
            const isCompleted = r.status === "COMPLETED";
            return (
              <div
                key={r.id}
                className="p-5 rounded-md bg-surface-card border border-surface-border hover:border-brand/40 transition-all flex flex-col justify-between space-y-4"
              >
                <div className="space-y-3">
                  <div className="flex items-start justify-between gap-2">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <Badge variant={r.roadmapType === "OPPORTUNITY_PREPARATION" ? "brand" : "cyan"}>
                          {r.roadmapType === "OPPORTUNITY_PREPARATION" ? "Opportunity Prep" : "Goal Mastery"}
                        </Badge>
                        <Badge variant={isCompleted ? "secondary" : "outline"} className={isCompleted ? "text-emerald-400" : ""}>
                          {r.status}
                        </Badge>
                      </div>
                      <h3 className="font-mono text-base font-bold text-text-primary pt-1">
                        {r.title}
                      </h3>
                    </div>
                  </div>

                  {r.targetOpportunityTitle && (
                    <div className="text-xs font-mono text-brand flex items-center gap-1.5">
                      <Target className="h-3.5 w-3.5" />
                      <span className="truncate">Target: {r.targetOpportunityTitle}</span>
                    </div>
                  )}

                  {r.targetGoal && !r.targetOpportunityTitle && (
                    <div className="text-xs font-mono text-cyan-400 flex items-center gap-1.5">
                      <Target className="h-3.5 w-3.5" />
                      <span className="truncate">Goal: {r.targetGoal}</span>
                    </div>
                  )}

                  {r.description && (
                    <p className="text-xs font-sans text-text-secondary line-clamp-2 leading-relaxed">
                      {r.description}
                    </p>
                  )}

                  {/* Progress bar */}
                  <div className="space-y-1.5 pt-1">
                    <div className="flex justify-between items-center text-[11px] font-mono">
                      <span className="text-text-muted">Progress</span>
                      <span className="font-bold text-text-primary">{Math.round(r.overallProgressPercentage)}%</span>
                    </div>
                    <div className="w-full bg-surface-border h-2 rounded-full overflow-hidden">
                      <div
                        className={`h-full rounded-full transition-all duration-300 ${isCompleted ? "bg-emerald-400" : "bg-brand"}`}
                        style={{ width: `${Math.min(100, Math.max(0, r.overallProgressPercentage))}%` }}
                      />
                    </div>
                  </div>

                  {/* Details metadata */}
                  <div className="pt-2 border-t border-surface-border/60 grid grid-cols-3 gap-2 text-[10px] font-mono text-text-muted">
                    <div>
                      <span className="block uppercase text-[9px]">Milestones</span>
                      <span className="font-semibold text-text-primary">
                        {r.completedMilestones}/{r.totalMilestones} Done
                      </span>
                    </div>
                    <div>
                      <span className="block uppercase text-[9px]">Tasks</span>
                      <span className="font-semibold text-text-primary">
                        {r.completedTasks}/{r.totalTasks} Done
                      </span>
                    </div>
                    <div>
                      <span className="block uppercase text-[9px]">Budget</span>
                      <span className="font-semibold text-text-primary">
                        {r.availableHoursPerWeek}h/wk
                      </span>
                    </div>
                  </div>
                </div>

                <div className="pt-2 flex items-center justify-between border-t border-surface-border/40">
                  <div className="text-[10px] font-mono text-text-muted flex items-center gap-1">
                    <Calendar className="h-3 w-3" />
                    <span>Target: {r.targetCompletionDate || "Flexible"}</span>
                  </div>
                  <Link href={`/roadmaps/${r.id}`}>
                    <Button variant="outline" size="sm" className="h-7 text-xs">
                      Execute Pathway
                      <ArrowRight className="ml-1.5 h-3.5 w-3.5" />
                    </Button>
                  </Link>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
