"use client";

import React, { useEffect, useState, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import {
  ArrowLeft,
  Sparkles,
  Target,
  Compass,
  Calendar,
  Clock,
  CheckCircle2,
  AlertCircle,
  Cpu,
  Layers,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { api } from "@/lib/api";
import { Opportunity, RoadmapType } from "@/lib/types";

function CreateRoadmapContent() {
  const router = useRouter();
  const searchParams = useSearchParams();

  const initialType = searchParams.get("type") === "goal" ? "GOAL_BASED" : "OPPORTUNITY_PREPARATION";
  const prefillOppId = searchParams.get("opportunityId");

  const [activeTab, setActiveTab] = useState<RoadmapType>(initialType);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Opportunity Tab State
  const [selectedOpportunity, setSelectedOpportunity] = useState<Opportunity | null>(null);
  const [oppSearch, setOppSearch] = useState("");
  const [searchedOpportunities, setSearchedOpportunities] = useState<Opportunity[]>([]);
  const [searchingOpp, setSearchingOpp] = useState(false);

  // Form Fields
  const [customTitle, setCustomTitle] = useState("");
  const [availableHours, setAvailableHours] = useState(10);
  const [targetDate, setTargetDate] = useState("");

  // Goal Tab State
  const [targetGoal, setTargetGoal] = useState("Full-Stack Software Engineer");
  const [targetDomain, setTargetDomain] = useState("Software Engineering");
  const [experienceLevel, setExperienceLevel] = useState("Intermediate");
  const [targetSkillsInput, setTargetSkillsInput] = useState("React, Spring Boot, PostgreSQL, Docker, Git");

  useEffect(() => {
    if (prefillOppId) {
      loadPrefilledOpportunity(prefillOppId);
    } else {
      loadInitialOpportunities();
    }
  }, [prefillOppId]);

  const loadPrefilledOpportunity = async (id: string) => {
    try {
      const opp = await api.opportunities.get(id);
      setSelectedOpportunity(opp);
      if (opp.deadline) {
        setTargetDate(opp.deadline);
      }
    } catch (e) {
      loadInitialOpportunities();
    }
  };

  const loadInitialOpportunities = async () => {
    try {
      setSearchingOpp(true);
      const res = await api.opportunities.list({ size: 6 });
      setSearchedOpportunities(res.content);
    } catch (e) {
      // Ignore
    } finally {
      setSearchingOpp(false);
    }
  };

  const handleSearchOpportunities = async (q: string) => {
    setOppSearch(q);
    try {
      setSearchingOpp(true);
      const res = await api.opportunities.list({ query: q, size: 8 });
      setSearchedOpportunities(res.content);
    } catch (e) {
      // Ignore
    } finally {
      setSearchingOpp(false);
    }
  };

  const handleGenerate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      if (activeTab === "OPPORTUNITY_PREPARATION") {
        if (!selectedOpportunity) {
          throw new Error("Please select an opportunity to generate a preparation roadmap.");
        }
        const created = await api.roadmaps.generate({
          roadmapType: "OPPORTUNITY_PREPARATION",
          opportunityId: selectedOpportunity.id,
          availableHoursPerWeek: Number(availableHours),
          targetCompletionDate: targetDate || undefined,
          customTitle: customTitle.trim() || undefined,
        });
        router.push(`/roadmaps/${created.id}`);
      } else {
        if (!targetGoal.trim()) {
          throw new Error("Please provide a career goal.");
        }
        const skillsArray = targetSkillsInput
          .split(",")
          .map((s) => s.trim())
          .filter(Boolean);

        const created = await api.roadmaps.generate({
          roadmapType: "GOAL_BASED",
          targetGoal: targetGoal.trim(),
          targetDomain: targetDomain.trim(),
          currentSkillLevel: experienceLevel,
          targetSkills: skillsArray,
          availableHoursPerWeek: Number(availableHours),
          targetCompletionDate: targetDate || undefined,
          customTitle: customTitle.trim() || undefined,
        });
        router.push(`/roadmaps/${created.id}`);
      }
    } catch (err: any) {
      setError(err?.message || "Failed to generate preparation roadmap");
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Back button */}
      <div>
        <Link
          href="/roadmaps"
          className="inline-flex items-center text-xs font-mono text-text-muted hover:text-text-primary transition-colors"
        >
          <ArrowLeft className="mr-1.5 h-3.5 w-3.5" /> Back to Roadmaps
        </Link>
      </div>

      {/* Header */}
      <div className="space-y-1">
        <h1 className="text-2xl font-mono font-bold text-text-primary">
          Generate Preparation Roadmap
        </h1>
        <p className="text-xs font-mono text-text-muted">
          Synthesize personalized learning milestones, technical practice criteria, and actionable schedules.
        </p>
      </div>

      {/* Mode Tabs */}
      <div className="flex border-b border-surface-border">
        <button
          type="button"
          onClick={() => setActiveTab("OPPORTUNITY_PREPARATION")}
          className={`px-4 py-2.5 font-mono text-xs font-semibold flex items-center gap-2 border-b-2 transition-colors ${
            activeTab === "OPPORTUNITY_PREPARATION"
              ? "border-brand text-brand"
              : "border-transparent text-text-muted hover:text-text-primary"
          }`}
        >
          <Sparkles className="h-4 w-4" />
          Prepare for an Opportunity
        </button>
        <button
          type="button"
          onClick={() => setActiveTab("GOAL_BASED")}
          className={`px-4 py-2.5 font-mono text-xs font-semibold flex items-center gap-2 border-b-2 transition-colors ${
            activeTab === "GOAL_BASED"
              ? "border-cyan-400 text-cyan-400"
              : "border-transparent text-text-muted hover:text-text-primary"
          }`}
        >
          <Target className="h-4 w-4" />
          Career &amp; Domain Goal
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-md bg-danger/10 border border-danger/30 text-xs font-mono text-danger flex items-center gap-2">
          <AlertCircle className="h-4 w-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Creation Form */}
      <form onSubmit={handleGenerate} className="space-y-6">
        {/* OPPORTUNITY MODE */}
        {activeTab === "OPPORTUNITY_PREPARATION" && (
          <div className="space-y-4">
            <div className="p-5 rounded-md bg-surface-card border border-surface-border space-y-4">
              <span className="text-[11px] font-mono uppercase tracking-wider text-text-secondary font-semibold block">
                1. Select Target Opportunity
              </span>

              {selectedOpportunity ? (
                <div className="p-4 rounded-md bg-bg-secondary border border-brand/30 space-y-2">
                  <div className="flex items-start justify-between">
                    <div>
                      <span className="text-xs font-mono uppercase text-text-muted block">
                        {selectedOpportunity.organization} · {selectedOpportunity.category}
                      </span>
                      <h4 className="font-mono text-sm font-bold text-text-primary">
                        {selectedOpportunity.title}
                      </h4>
                    </div>
                    <Button
                      variant="ghost"
                      size="sm"
                      type="button"
                      onClick={() => setSelectedOpportunity(null)}
                      className="text-xs text-text-muted hover:text-danger h-7"
                    >
                      Change
                    </Button>
                  </div>

                  {selectedOpportunity.requiredSkills && selectedOpportunity.requiredSkills.length > 0 && (
                    <div className="flex flex-wrap gap-1 pt-1">
                      {selectedOpportunity.requiredSkills.map((s) => (
                        <Badge key={s} variant="secondary">
                          {s}
                        </Badge>
                      ))}
                    </div>
                  )}

                  {selectedOpportunity.deadline && (
                    <div className="text-[11px] font-mono text-text-muted flex items-center gap-1 pt-1">
                      <Calendar className="h-3 w-3" />
                      <span>Application Deadline: {selectedOpportunity.deadline}</span>
                    </div>
                  )}
                </div>
              ) : (
                <div className="space-y-3">
                  <Input
                    placeholder="Search opportunities by title, organization, or category..."
                    value={oppSearch}
                    onChange={(e) => handleSearchOpportunities(e.target.value)}
                    className="h-9 text-xs font-mono"
                  />

                  {searchingOpp && (
                    <div className="text-xs font-mono text-text-muted">Searching catalog...</div>
                  )}

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 pt-1 max-h-60 overflow-y-auto">
                    {searchedOpportunities.map((opp) => (
                      <div
                        key={opp.id}
                        onClick={() => {
                          setSelectedOpportunity(opp);
                          if (opp.deadline) setTargetDate(opp.deadline);
                        }}
                        className="p-3 rounded-sm bg-bg-secondary border border-surface-border hover:border-brand/50 cursor-pointer transition-colors space-y-1"
                      >
                        <span className="text-[10px] font-mono uppercase text-text-muted block truncate">
                          {opp.organization}
                        </span>
                        <h5 className="font-mono text-xs font-semibold text-text-primary truncate">
                          {opp.title}
                        </h5>
                        <div className="flex items-center gap-2 text-[10px] font-mono text-text-muted">
                          <Badge variant="outline" className="text-[9px] py-0">
                            {opp.category}
                          </Badge>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
        )}

        {/* GOAL-BASED MODE */}
        {activeTab === "GOAL_BASED" && (
          <div className="space-y-4">
            <div className="p-5 rounded-md bg-surface-card border border-surface-border space-y-4">
              <span className="text-[11px] font-mono uppercase tracking-wider text-text-secondary font-semibold block">
                1. Define Your Career or Technical Goal
              </span>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-1.5">
                  <label className="text-xs font-mono text-text-secondary block">
                    Target Role / Goal:
                  </label>
                  <Input
                    value={targetGoal}
                    onChange={(e) => setTargetGoal(e.target.value)}
                    placeholder="e.g. Distributed Systems Engineer"
                    className="h-9 text-xs font-mono"
                    required
                  />
                </div>

                <div className="space-y-1.5">
                  <label className="text-xs font-mono text-text-secondary block">
                    Domain / Focus Area:
                  </label>
                  <select
                    value={targetDomain}
                    onChange={(e) => setTargetDomain(e.target.value)}
                    className="w-full h-9 rounded-sm border border-surface-border bg-bg-secondary px-3 text-xs font-mono text-text-primary focus:outline-none focus:ring-1 focus:ring-brand"
                  >
                    <option value="Software Engineering">Software Engineering</option>
                    <option value="Backend Development">Backend Development</option>
                    <option value="Frontend & Web Development">Frontend &amp; Web Development</option>
                    <option value="Cloud & DevOps Engineering">Cloud &amp; DevOps Engineering</option>
                    <option value="Systems & Security">Systems &amp; Security</option>
                    <option value="Machine Learning & AI">Machine Learning &amp; AI</option>
                    <option value="Open Source Architecture">Open Source Architecture</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
                <div className="space-y-1.5">
                  <label className="text-xs font-mono text-text-secondary block">
                    Current Experience Level:
                  </label>
                  <select
                    value={experienceLevel}
                    onChange={(e) => setExperienceLevel(e.target.value)}
                    className="w-full h-9 rounded-sm border border-surface-border bg-bg-secondary px-3 text-xs font-mono text-text-primary focus:outline-none focus:ring-1 focus:ring-brand"
                  >
                    <option value="Beginner">Beginner (Foundations &amp; Basics)</option>
                    <option value="Intermediate">Intermediate (Practical Projects &amp; Frameworks)</option>
                    <option value="Advanced">Advanced (Systems Architecture &amp; Scale)</option>
                  </select>
                </div>

                <div className="space-y-1.5">
                  <label className="text-xs font-mono text-text-secondary block">
                    Target Technologies (comma-separated):
                  </label>
                  <Input
                    value={targetSkillsInput}
                    onChange={(e) => setTargetSkillsInput(e.target.value)}
                    placeholder="e.g. Go, Kubernetes, PostgreSQL, Docker"
                    className="h-9 text-xs font-mono"
                  />
                </div>
              </div>
            </div>
          </div>
        )}

        {/* SHARED SCHEDULE & PACE CONFIGURATION */}
        <div className="p-5 rounded-md bg-surface-card border border-surface-border space-y-4">
          <span className="text-[11px] font-mono uppercase tracking-wider text-text-secondary font-semibold block">
            2. Schedule, Budget &amp; Target Pace
          </span>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <div className="flex justify-between items-center text-xs font-mono">
                <label className="text-text-secondary">Weekly Time Budget:</label>
                <span className="font-bold text-brand">{availableHours} Hours / Week</span>
              </div>
              <input
                type="range"
                min="3"
                max="40"
                step="1"
                value={availableHours}
                onChange={(e) => setAvailableHours(Number(e.target.value))}
                className="w-full accent-brand cursor-pointer"
              />
              <span className="text-[10px] font-mono text-text-muted block">
                Deterministic pacing: ~{(availableHours / 5).toFixed(1)} hours/day (5-day study week).
              </span>
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-mono text-text-secondary block">
                Target Completion Deadline (Optional):
              </label>
              <Input
                type="date"
                value={targetDate}
                onChange={(e) => setTargetDate(e.target.value)}
                className="h-9 text-xs font-mono"
              />
              <span className="text-[10px] font-mono text-text-muted block">
                Leave blank to auto-calculate based on weekly hours.
              </span>
            </div>
          </div>

          <div className="space-y-1.5 pt-2">
            <label className="text-xs font-mono text-text-secondary block">
              Custom Roadmap Title (Optional):
            </label>
            <Input
              value={customTitle}
              onChange={(e) => setCustomTitle(e.target.value)}
              placeholder="e.g. Sprint to Google Summer of Code 2027"
              className="h-9 text-xs font-mono"
            />
          </div>
        </div>

        {/* Action Button */}
        <div className="flex items-center justify-end gap-3 pt-2">
          <Link href="/roadmaps">
            <Button variant="outline" size="sm" type="button">
              Cancel
            </Button>
          </Link>
          <Button variant="primary" size="sm" type="submit" loading={loading}>
            <Sparkles className="mr-1.5 h-3.5 w-3.5" />
            Synthesize Strategic Roadmap
          </Button>
        </div>
      </form>
    </div>
  );
}

export default function CreateRoadmapPage() {
  return (
    <Suspense fallback={<div className="font-mono text-xs text-text-muted p-6">Loading roadmap wizard...</div>}>
      <CreateRoadmapContent />
    </Suspense>
  );
}
