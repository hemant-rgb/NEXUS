"use client";

import React, { useState, useEffect, useCallback } from "react";
import Link from "next/link";
import {
  Sparkles,
  RefreshCw,
  Filter,
  ShieldCheck,
  AlertTriangle,
  HelpCircle,
  XCircle,
  ArrowUpRight,
  SlidersHorizontal,
  Calendar,
  CheckCircle2,
  Building,
  MapPin,
} from "lucide-react";
import { api } from "@/lib/api";
import { OpportunityMatch, OPPORTUNITY_CATEGORIES } from "@/lib/types";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";

export default function RecommendationsPage() {
  const [matches, setMatches] = useState<OpportunityMatch[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Filters
  const [minScore, setMinScore] = useState<number>(0);
  const [selectedCategory, setSelectedCategory] = useState<string>("");
  const [eligibilityStatus, setEligibilityStatus] = useState<string>("");
  const [sortBy, setSortBy] = useState<string>("RELEVANCE");

  const fetchRecommendations = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.matching.getRecommendations({
        minScore: minScore > 0 ? minScore : undefined,
        category: selectedCategory || undefined,
        eligibilityStatus: eligibilityStatus || undefined,
        sortBy,
        page: 0,
        size: 30,
      });
      setMatches(res.content || []);
    } catch (err: any) {
      setError(err?.message || "Failed to load personalized recommendations.");
    } finally {
      setLoading(false);
    }
  }, [minScore, selectedCategory, eligibilityStatus, sortBy]);

  useEffect(() => {
    fetchRecommendations();
  }, [fetchRecommendations]);

  const handleRefresh = async () => {
    setRefreshing(true);
    try {
      await api.matching.refreshMatches();
      await fetchRecommendations();
    } catch {
      alert("Failed to refresh matching intelligence.");
    } finally {
      setRefreshing(false);
    }
  };

  const getScoreColor = (score: number) => {
    if (score >= 80) return "text-brand border-brand/40 bg-brand/10";
    if (score >= 65) return "text-cyan-400 border-cyan-500/40 bg-cyan-950/20";
    if (score >= 50) return "text-amber-400 border-amber-500/40 bg-amber-950/20";
    return "text-red-400 border-red-500/40 bg-red-950/20";
  };

  const renderEligibilityBadge = (status: string) => {
    switch (status) {
      case "ELIGIBLE":
        return (
          <Badge variant="outline" className="border-brand/40 text-brand bg-brand/10 flex items-center gap-1">
            <ShieldCheck className="h-3 w-3" /> Eligible
          </Badge>
        );
      case "NEEDS_REVIEW":
        return (
          <Badge variant="outline" className="border-amber-500/40 text-amber-400 bg-amber-950/20 flex items-center gap-1">
            <AlertTriangle className="h-3 w-3" /> Needs Review
          </Badge>
        );
      case "INELIGIBLE":
        return (
          <Badge variant="outline" className="border-red-500/40 text-red-400 bg-red-950/20 flex items-center gap-1">
            <XCircle className="h-3 w-3" /> Ineligible
          </Badge>
        );
      default:
        return (
          <Badge variant="outline" className="border-text-muted/40 text-text-muted bg-surface-card flex items-center gap-1">
            <HelpCircle className="h-3 w-3" /> Unknown Criteria
          </Badge>
        );
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-surface-border">
        <div>
          <div className="flex items-center gap-2">
            <Sparkles className="h-5 w-5 text-brand" />
            <h1 className="text-xl font-mono font-bold text-text-primary tracking-tight">
              Personalized Recommendations
            </h1>
          </div>
          <p className="text-xs font-mono text-text-muted mt-1">
            Deterministic multi-dimensional matching engine combining skills, eligibility, interests, experience, and deadlines.
          </p>
        </div>

        <Button
          variant="outline"
          size="sm"
          loading={refreshing}
          onClick={handleRefresh}
          className="shrink-0"
        >
          <RefreshCw className="mr-1.5 h-3.5 w-3.5" /> Refresh Intelligence
        </Button>
      </div>

      {/* Filter Control Bar */}
      <div className="p-4 rounded-md bg-surface-card border border-surface-border space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-3 text-xs font-mono">
          <div className="flex flex-wrap items-center gap-2">
            <span className="text-text-muted text-[11px] uppercase tracking-wider flex items-center gap-1">
              <SlidersHorizontal className="h-3.5 w-3.5" /> Min Score:
            </span>
            {[
              { label: "All", val: 0 },
              { label: "50%+", val: 50 },
              { label: "70%+", val: 70 },
              { label: "80%+", val: 80 },
              { label: "90%+", val: 90 },
            ].map((s) => (
              <button
                key={s.label}
                onClick={() => setMinScore(s.val)}
                className={`px-2.5 py-1 rounded-sm border transition-colors ${
                  minScore === s.val
                    ? "bg-brand/15 text-brand border-brand font-medium"
                    : "bg-bg-secondary text-text-secondary border-surface-border hover:border-text-secondary"
                }`}
              >
                {s.label}
              </button>
            ))}
          </div>

          <div className="flex items-center gap-3">
            {/* Eligibility filter */}
            <select
              value={eligibilityStatus}
              onChange={(e) => setEligibilityStatus(e.target.value)}
              className="px-2.5 py-1.5 rounded-sm bg-bg-secondary border border-surface-border text-text-secondary text-xs font-mono focus:border-brand focus:outline-none"
            >
              <option value="">All Eligibility</option>
              <option value="ELIGIBLE">Eligible Only</option>
              <option value="NEEDS_REVIEW">Needs Review</option>
              <option value="UNKNOWN">Unknown Criteria</option>
              <option value="INELIGIBLE">Ineligible</option>
            </select>

            {/* Sort Order */}
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="px-2.5 py-1.5 rounded-sm bg-bg-secondary border border-surface-border text-text-secondary text-xs font-mono focus:border-brand focus:outline-none"
            >
              <option value="RELEVANCE">Highest Match Score</option>
              <option value="DEADLINE">Earliest Deadline</option>
            </select>
          </div>
        </div>

        {/* Categories Pills */}
        <div className="pt-2 border-t border-surface-border/60 flex flex-wrap gap-1.5 items-center">
          <button
            onClick={() => setSelectedCategory("")}
            className={`px-2 py-0.5 rounded-sm text-[11px] font-mono border transition-colors ${
              selectedCategory === ""
                ? "bg-brand text-black border-brand font-semibold"
                : "bg-bg-secondary text-text-muted border-surface-border hover:text-text-primary"
            }`}
          >
            All Tracks
          </button>
          {OPPORTUNITY_CATEGORIES.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat === selectedCategory ? "" : cat)}
              className={`px-2 py-0.5 rounded-sm text-[11px] font-mono border transition-colors ${
                selectedCategory === cat
                  ? "bg-brand text-black border-brand font-semibold"
                  : "bg-bg-secondary text-text-muted border-surface-border hover:text-text-primary"
              }`}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      {/* Error state */}
      {error && (
        <div className="p-4 rounded-md bg-danger/10 border border-danger/30 text-xs font-mono text-danger">
          {error}
        </div>
      )}

      {/* Loading state */}
      {loading ? (
        <div className="flex h-72 items-center justify-center font-mono text-xs text-text-muted">
          Evaluating intelligence models and computing profile vectors...
        </div>
      ) : matches.length === 0 ? (
        <div className="p-12 text-center rounded-md border border-dashed border-surface-border space-y-3">
          <Sparkles className="h-8 w-8 text-text-muted mx-auto" />
          <h3 className="font-mono text-sm font-semibold text-text-primary">
            No recommendations match your filters
          </h3>
          <p className="text-xs font-mono text-text-muted max-w-md mx-auto">
            Try adjusting your minimum match score or selected category to see more opportunities.
          </p>
          <Button
            variant="outline"
            size="sm"
            onClick={() => {
              setMinScore(0);
              setSelectedCategory("");
              setEligibilityStatus("");
            }}
          >
            Reset Filters
          </Button>
        </div>
      ) : (
        /* Results Grid */
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {matches.map((m) => {
            const opp = m.opportunity;
            return (
              <div
                key={m.id}
                className="p-5 rounded-md bg-surface-card border border-surface-border hover:border-surface-border/90 flex flex-col justify-between space-y-4 group transition-all"
              >
                {/* Header Strip with Score & Eligibility */}
                <div className="space-y-3">
                  <div className="flex items-start justify-between gap-3">
                    <div className="space-y-1 min-w-0">
                      <span className="text-[11px] font-mono uppercase tracking-wider text-text-muted block truncate">
                        {opp.organization}
                      </span>
                      <Link href={`/opportunities/${opp.id}`} className="block">
                        <h3 className="font-mono text-sm font-semibold text-text-primary group-hover:text-brand transition-colors line-clamp-1">
                          {opp.title}
                        </h3>
                      </Link>
                    </div>

                    {/* Overall Score Dial */}
                    <div
                      className={`px-2.5 py-1 rounded-sm border font-mono font-bold text-xs shrink-0 flex items-center gap-1 ${getScoreColor(
                        m.overallScore
                      )}`}
                    >
                      <Sparkles className="h-3 w-3" />
                      <span>{Math.round(m.overallScore)}%</span>
                    </div>
                  </div>

                  {/* Badges: Category, Eligibility, Remote */}
                  <div className="flex flex-wrap items-center gap-1.5">
                    <Badge variant="cyan">{opp.category}</Badge>
                    {renderEligibilityBadge(m.eligibility.status)}
                    {opp.remoteAvailable && <Badge variant="brand">Remote</Badge>}
                  </div>

                  {/* Dimension Micro-bars */}
                  <div className="p-2.5 rounded-sm bg-bg-secondary/70 border border-surface-border/60 space-y-1.5 text-[10px] font-mono">
                    <div className="flex items-center justify-between text-text-muted">
                      <span>Skills Coverage (30%)</span>
                      <span className="text-text-primary font-medium">{Math.round(m.dimensionScores.skills)}%</span>
                    </div>
                    <div className="w-full bg-surface-border h-1 rounded-full overflow-hidden">
                      <div
                        className="bg-brand h-full rounded-full"
                        style={{ width: `${Math.min(100, m.dimensionScores.skills)}%` }}
                      />
                    </div>

                    <div className="flex items-center justify-between text-text-muted pt-1">
                      <span>Eligibility Fit (25%)</span>
                      <span className="text-text-primary font-medium">{Math.round(m.dimensionScores.eligibility)}%</span>
                    </div>
                    <div className="w-full bg-surface-border h-1 rounded-full overflow-hidden">
                      <div
                        className="bg-cyan-400 h-full rounded-full"
                        style={{ width: `${Math.min(100, m.dimensionScores.eligibility)}%` }}
                      />
                    </div>
                  </div>

                  {/* Recommendation Reason Summary */}
                  <p className="text-xs font-sans text-text-secondary leading-relaxed line-clamp-2 italic">
                    &ldquo;{m.recommendationReason}&rdquo;
                  </p>
                </div>

                {/* Footer with Matched / Missing Skills & Actions */}
                <div className="space-y-3 pt-3 border-t border-surface-border/60 text-xs font-mono">
                  {/* Top Matching Skills */}
                  {m.matchedSkills && m.matchedSkills.length > 0 && (
                    <div className="flex flex-wrap gap-1 items-center">
                      <span className="text-[10px] text-text-muted mr-1">Matching:</span>
                      {m.matchedSkills.slice(0, 3).map((s) => (
                        <span
                          key={s}
                          className="px-1.5 py-0.5 rounded-sm bg-brand/10 border border-brand/30 text-[10px] text-brand"
                        >
                          {s}
                        </span>
                      ))}
                      {m.matchedSkills.length > 3 && (
                        <span className="text-[10px] text-text-muted">+{m.matchedSkills.length - 3}</span>
                      )}
                    </div>
                  )}

                  {/* Missing Required Skills */}
                  {m.missingRequiredSkills && m.missingRequiredSkills.length > 0 && (
                    <div className="flex flex-wrap gap-1 items-center">
                      <span className="text-[10px] text-text-muted mr-1">Missing:</span>
                      {m.missingRequiredSkills.slice(0, 2).map((s) => (
                        <span
                          key={s}
                          className="px-1.5 py-0.5 rounded-sm bg-red-950/20 border border-red-500/30 text-[10px] text-red-400"
                        >
                          {s}
                        </span>
                      ))}
                      {m.missingRequiredSkills.length > 2 && (
                        <span className="text-[10px] text-text-muted">+{m.missingRequiredSkills.length - 2}</span>
                      )}
                    </div>
                  )}

                  {/* Action Link */}
                  <div className="flex items-center justify-between pt-2">
                    <div className="flex items-center gap-1.5 text-text-muted text-[11px]">
                      <Calendar className="h-3 w-3" />
                      <span>{opp.deadline ? opp.deadline : "Rolling"}</span>
                    </div>

                    <Link href={`/opportunities/${opp.id}`}>
                      <Button variant="outline" size="sm" className="h-7 px-2.5 text-[11px]">
                        Inspect Match <ArrowUpRight className="ml-1 h-3 w-3" />
                      </Button>
                    </Link>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
