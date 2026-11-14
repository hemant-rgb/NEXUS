"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import {
  ArrowLeft,
  Calendar,
  MapPin,
  ExternalLink,
  Bookmark,
  BookmarkCheck,
  ShieldCheck,
  AlertTriangle,
  HelpCircle,
  XCircle,
  Sparkles,
  Bot,
  BrainCircuit,
  Save,
  AlertCircle,
  CheckCircle2,
  ChevronRight,
  RefreshCw,
  Layers,
} from "lucide-react";
import { api } from "@/lib/api";
import {
  Opportunity,
  OpportunityMatch,
  SkillGapReport,
  OpportunityAnalysis,
} from "@/lib/types";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";

export default function OpportunityDetailsPage() {
  const params = useParams();
  const router = useRouter();
  const id = params?.id as string;

  const [opportunity, setOpportunity] = useState<Opportunity | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Saved state & notes
  const [isSaved, setIsSaved] = useState(false);
  const [notes, setNotes] = useState("");
  const [savingNote, setSavingNote] = useState(false);
  const [noteSuccess, setNoteSuccess] = useState(false);

  // Phase 4: Matching & Intelligence State
  const [match, setMatch] = useState<OpportunityMatch | null>(null);
  const [skillGap, setSkillGap] = useState<SkillGapReport | null>(null);
  const [analysis, setAnalysis] = useState<OpportunityAnalysis | null>(null);
  const [analyzing, setAnalyzing] = useState(false);
  const [analysisError, setAnalysisError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchOpportunity() {
      if (!id) return;
      setLoading(true);
      try {
        const data = await api.opportunities.get(id);
        setOpportunity(data);
        setIsSaved(data.isSaved || false);

        // Fetch notes if saved
        if (data.isSaved) {
          const savedList = await api.opportunities.saved().catch(() => []);
          const matched = savedList.find((s) => s.opportunityId === id);
          if (matched && matched.notes) {
            setNotes(matched.notes);
          }
        }

        // Fetch Matching & Skill Gap
        api.matching.getMatch(id).then(setMatch).catch(() => {});
        api.matching.getSkillGap(id).then(setSkillGap).catch(() => {});

        // Fetch cached AI analysis if available
        api.intelligence.getAnalysis(id).then(setAnalysis).catch(() => {});
      } catch (err: any) {
        setError(err?.message || "Failed to load opportunity details");
      } finally {
        setLoading(false);
      }
    }
    fetchOpportunity();
  }, [id]);

  const handleToggleSave = async () => {
    if (!opportunity) return;
    try {
      if (isSaved) {
        await api.opportunities.removeSaved(opportunity.id);
        setIsSaved(false);
      } else {
        await api.opportunities.save(opportunity.id, notes);
        setIsSaved(true);
      }
    } catch {
      alert("Failed to update bookmark status");
    }
  };

  const handleSaveNotes = async () => {
    if (!opportunity) return;
    setSavingNote(true);
    setNoteSuccess(false);
    try {
      if (isSaved) {
        await api.opportunities.updateNotes(opportunity.id, notes);
      } else {
        await api.opportunities.save(opportunity.id, notes);
        setIsSaved(true);
      }
      setNoteSuccess(true);
      setTimeout(() => setNoteSuccess(false), 3000);
    } catch {
      alert("Failed to save personal notes");
    } finally {
      setSavingNote(false);
    }
  };

  const handleGenerateAnalysis = async () => {
    if (!opportunity) return;
    setAnalyzing(true);
    setAnalysisError(null);
    try {
      const res = await api.intelligence.generateAnalysis(opportunity.id);
      setAnalysis(res);
    } catch (err: any) {
      setAnalysisError(err?.message || "Failed to generate AI intelligence analysis.");
    } finally {
      setAnalyzing(false);
    }
  };

  const renderEligibilityBadge = (status?: string) => {
    switch (status) {
      case "ELIGIBLE":
        return (
          <span className="inline-flex items-center gap-1 text-brand font-medium">
            <ShieldCheck className="h-3.5 w-3.5" /> Eligible
          </span>
        );
      case "NEEDS_REVIEW":
        return (
          <span className="inline-flex items-center gap-1 text-amber-400 font-medium">
            <AlertTriangle className="h-3.5 w-3.5" /> Needs Review
          </span>
        );
      case "INELIGIBLE":
        return (
          <span className="inline-flex items-center gap-1 text-red-400 font-medium">
            <XCircle className="h-3.5 w-3.5" /> Ineligible
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 text-text-muted">
            <HelpCircle className="h-3.5 w-3.5" /> Unknown Criteria
          </span>
        );
    }
  };

  const renderRecommendationBadge = (rec: string) => {
    switch (rec) {
      case "STRONG_FIT":
        return <Badge variant="brand">Strong Strategic Fit</Badge>;
      case "POTENTIAL_FIT":
        return <Badge variant="cyan">Potential Strategic Fit</Badge>;
      case "WEAK_FIT":
        return <Badge variant="outline" className="border-red-500/40 text-red-400">Weak Fit</Badge>;
      default:
        return <Badge variant="secondary">Needs More Information</Badge>;
    }
  };

  if (loading) {
    return (
      <div className="flex h-96 items-center justify-center font-mono text-xs text-text-muted">
        Loading opportunity specification and intelligence models...
      </div>
    );
  }

  if (error || !opportunity) {
    return (
      <div className="max-w-4xl mx-auto p-6 space-y-4">
        <div className="p-4 rounded-md bg-danger/10 border border-danger/30 text-xs font-mono text-danger flex items-center gap-2">
          <AlertCircle className="h-4 w-4" />
          <span>{error || "Opportunity not found."}</span>
        </div>
        <Link href="/opportunities">
          <Button variant="outline" size="sm">
            <ArrowLeft className="mr-1.5 h-3.5 w-3.5" /> Back to Discovery
          </Button>
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      {/* Back Button */}
      <div className="flex items-center justify-between">
        <Link href="/opportunities" className="inline-flex items-center text-xs font-mono text-text-muted hover:text-text-primary transition-colors">
          <ArrowLeft className="mr-1.5 h-3.5 w-3.5" /> Back to Opportunity Feed
        </Link>
        <Link href="/recommendations" className="inline-flex items-center text-xs font-mono text-brand hover:underline">
          <Sparkles className="mr-1.5 h-3.5 w-3.5" /> View All Personalized Recommendations
        </Link>
      </div>

      {/* Main Header Card */}
      <div className="p-6 rounded-md bg-surface-card border border-surface-border space-y-4">
        <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
          <div className="space-y-1.5">
            <div className="flex items-center gap-2">
              <span className="text-xs font-mono uppercase tracking-wider text-text-secondary font-medium">
                {opportunity.organization}
              </span>
              <span className="text-text-muted">·</span>
              <Badge variant="cyan">{opportunity.category}</Badge>
              {opportunity.remoteAvailable && <Badge variant="brand">Remote</Badge>}
            </div>
            <h1 className="text-2xl font-mono font-bold text-text-primary">
              {opportunity.title}
            </h1>
          </div>

          <div className="flex items-center gap-3 shrink-0">
            <Link href={`/roadmaps/new?opportunityId=${opportunity.id}`}>
              <Button variant="outline" size="sm" className="border-brand/40 text-brand hover:bg-brand/10">
                <Sparkles className="mr-1.5 h-3.5 w-3.5 text-brand" />
                Prepare Roadmap
              </Button>
            </Link>

            <Button
              variant={isSaved ? "secondary" : "outline"}
              size="sm"
              onClick={handleToggleSave}
            >
              {isSaved ? (
                <>
                  <BookmarkCheck className="mr-1.5 h-4 w-4 text-brand" /> Saved
                </>
              ) : (
                <>
                  <Bookmark className="mr-1.5 h-4 w-4" /> Save Opportunity
                </>
              )}
            </Button>

            <a
              href={opportunity.applicationUrl}
              target="_blank"
              rel="noopener noreferrer"
            >
              <Button variant="primary" size="sm">
                Apply on Official Site
                <ExternalLink className="ml-1.5 h-3.5 w-3.5" />
              </Button>
            </a>
          </div>
        </div>

        {/* Quick Highlights Strip */}
        <div className="pt-4 border-t border-surface-border/60 grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs font-mono">
          <div>
            <span className="text-text-muted block text-[10px] uppercase tracking-wider">Application Deadline</span>
            <span className="font-semibold text-text-primary">
              {opportunity.deadline || "Rolling"} ({opportunity.deadlineType})
            </span>
          </div>

          <div>
            <span className="text-text-muted block text-[10px] uppercase tracking-wider">Location / Territory</span>
            <span className="font-semibold text-text-primary">
              {opportunity.location || "Global / Online"}
            </span>
          </div>

          <div>
            <span className="text-text-muted block text-[10px] uppercase tracking-wider">Program Dates</span>
            <span className="font-semibold text-text-primary">
              {opportunity.startDate ? `${opportunity.startDate} → ${opportunity.endDate || "Ongoing"}` : "Flexible"}
            </span>
          </div>

          <div>
            <span className="text-text-muted block text-[10px] uppercase tracking-wider">Eligibility Status</span>
            <span className="font-semibold flex items-center gap-1">
              {renderEligibilityBadge(match?.eligibility.status)}
            </span>
          </div>
        </div>
      </div>

      {/* PHASE 4: PERSONALIZED MATCH INTELLIGENCE CARD */}
      {match && (
        <div className="p-6 rounded-md bg-surface-card border border-brand/30 shadow-sm space-y-5">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-surface-border/60">
            <div className="flex items-center gap-2">
              <Sparkles className="h-5 w-5 text-brand" />
              <h2 className="font-mono text-sm font-bold text-text-primary tracking-wide">
                Personalized Match Intelligence
              </h2>
            </div>

            <div className="flex items-center gap-2 font-mono">
              <span className="text-xs text-text-muted">Relevance Score:</span>
              <span className="px-2.5 py-0.5 rounded-sm font-bold text-sm bg-brand/15 text-brand border border-brand/40">
                {Math.round(match.overallScore)}% Match
              </span>
            </div>
          </div>

          {/* 6 Dimensions Breakdown Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3 text-xs font-mono">
            {[
              { label: "Skills (30%)", score: match.dimensionScores.skills, color: "bg-brand" },
              { label: "Eligibility (25%)", score: match.dimensionScores.eligibility, color: "bg-cyan-400" },
              { label: "Interest (15%)", score: match.dimensionScores.interest, color: "bg-purple-400" },
              { label: "Experience (15%)", score: match.dimensionScores.experience, color: "bg-blue-400" },
              { label: "Deadline (10%)", score: match.dimensionScores.deadline, color: "bg-amber-400" },
              { label: "Location (5%)", score: match.dimensionScores.location, color: "bg-emerald-400" },
            ].map((d) => (
              <div key={d.label} className="p-2.5 rounded-sm bg-bg-secondary border border-surface-border space-y-1.5">
                <div className="flex justify-between items-center text-[10px] text-text-muted">
                  <span className="truncate">{d.label}</span>
                  <span className="text-text-primary font-semibold">{Math.round(d.score)}%</span>
                </div>
                <div className="w-full bg-surface-border h-1.5 rounded-full overflow-hidden">
                  <div className={`${d.color} h-full rounded-full`} style={{ width: `${Math.min(100, d.score)}%` }} />
                </div>
              </div>
            ))}
          </div>

          {/* Recommendation Reason & Explanations */}
          <div className="p-3.5 rounded-sm bg-bg-secondary/60 border border-surface-border text-xs font-mono space-y-2">
            <div className="text-text-primary font-semibold flex items-center gap-1.5">
              <span>Recommendation Summary:</span>
            </div>
            <p className="text-xs font-sans text-text-secondary leading-relaxed">
              {match.recommendationReason}
            </p>

            <div className="pt-2 grid grid-cols-1 md:grid-cols-2 gap-3 text-[11px]">
              {/* Positive Factors */}
              {match.positiveFactors && match.positiveFactors.length > 0 && (
                <div className="space-y-1">
                  <span className="text-brand font-semibold block uppercase tracking-wider text-[10px]">
                    Strengths &amp; Alignment:
                  </span>
                  <ul className="space-y-1 text-text-secondary">
                    {match.positiveFactors.map((f, i) => (
                      <li key={i} className="flex items-start gap-1.5">
                        <CheckCircle2 className="h-3 w-3 text-brand shrink-0 mt-0.5" />
                        <span>{f}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {/* Negative / Caution Factors */}
              {match.negativeFactors && match.negativeFactors.length > 0 && (
                <div className="space-y-1">
                  <span className="text-amber-400 font-semibold block uppercase tracking-wider text-[10px]">
                    Considerations &amp; Gaps:
                  </span>
                  <ul className="space-y-1 text-text-secondary">
                    {match.negativeFactors.map((f, i) => (
                      <li key={i} className="flex items-start gap-1.5">
                        <AlertTriangle className="h-3 w-3 text-amber-400 shrink-0 mt-0.5" />
                        <span>{f}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* PHASE 4: SKILL GAP MATRIX */}
      {skillGap && (
        <div className="p-5 rounded-md bg-surface-card border border-surface-border space-y-4">
          <div className="flex items-center justify-between pb-2 border-b border-surface-border/60">
            <div className="flex items-center gap-2">
              <Layers className="h-4 w-4 text-cyan-400" />
              <h3 className="font-mono text-xs uppercase tracking-wider text-text-secondary font-semibold">
                Technical Skill Gap Matrix ({Math.round(skillGap.matchPercentage)}% Match)
              </h3>
            </div>
            <span className="text-xs font-mono text-text-muted">
              {skillGap.totalSkillsEvaluated} Total Competencies Evaluated
            </span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 text-xs font-mono">
            {/* Matched Skills */}
            <div className="p-3 rounded-sm bg-bg-secondary border border-surface-border space-y-2">
              <span className="text-[10px] uppercase tracking-wider text-brand font-semibold block">
                Matched ({skillGap.matchedSkills.length})
              </span>
              <div className="flex flex-wrap gap-1">
                {skillGap.matchedSkills.length === 0 ? (
                  <span className="text-text-muted text-[11px]">None listed</span>
                ) : (
                  skillGap.matchedSkills.map((s) => (
                    <span key={s.skillName} className="px-2 py-0.5 rounded-sm bg-brand/10 border border-brand/30 text-brand text-[10px]">
                      {s.skillName} ({s.userProficiency})
                    </span>
                  ))
                )}
              </div>
            </div>

            {/* Partially Matched */}
            <div className="p-3 rounded-sm bg-bg-secondary border border-surface-border space-y-2">
              <span className="text-[10px] uppercase tracking-wider text-cyan-400 font-semibold block">
                Partial Match ({skillGap.partiallyMatchedSkills.length})
              </span>
              <div className="flex flex-wrap gap-1">
                {skillGap.partiallyMatchedSkills.length === 0 ? (
                  <span className="text-text-muted text-[11px]">None listed</span>
                ) : (
                  skillGap.partiallyMatchedSkills.map((s) => (
                    <span key={s.skillName} className="px-2 py-0.5 rounded-sm bg-cyan-950/20 border border-cyan-500/30 text-cyan-300 text-[10px]">
                      {s.skillName} ({s.userProficiency || "Beginner"})
                    </span>
                  ))
                )}
              </div>
            </div>

            {/* Missing Required */}
            <div className="p-3 rounded-sm bg-bg-secondary border border-surface-border space-y-2">
              <span className="text-[10px] uppercase tracking-wider text-red-400 font-semibold block">
                Missing Required ({skillGap.missingRequiredSkills.length})
              </span>
              <div className="flex flex-wrap gap-1">
                {skillGap.missingRequiredSkills.length === 0 ? (
                  <span className="text-text-muted text-[11px]">Zero gaps</span>
                ) : (
                  skillGap.missingRequiredSkills.map((s) => (
                    <span key={s.skillName} className="px-2 py-0.5 rounded-sm bg-red-950/20 border border-red-500/30 text-red-400 text-[10px]">
                      {s.skillName} (High)
                    </span>
                  ))
                )}
              </div>
            </div>

            {/* Missing Preferred */}
            <div className="p-3 rounded-sm bg-bg-secondary border border-surface-border space-y-2">
              <span className="text-[10px] uppercase tracking-wider text-text-muted font-semibold block">
                Missing Preferred ({skillGap.missingPreferredSkills.length})
              </span>
              <div className="flex flex-wrap gap-1">
                {skillGap.missingPreferredSkills.length === 0 ? (
                  <span className="text-text-muted text-[11px]">Zero gaps</span>
                ) : (
                  skillGap.missingPreferredSkills.map((s) => (
                    <span key={s.skillName} className="px-2 py-0.5 rounded-sm bg-bg-main border border-surface-border text-text-muted text-[10px]">
                      {s.skillName}
                    </span>
                  ))
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* PHASE 4: NEXUS INTELLIGENCE (AI ANALYSIS SECTION) */}
      <div className="p-6 rounded-md bg-surface-card border border-surface-border space-y-5">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-surface-border/60">
          <div className="flex items-center gap-2">
            <BrainCircuit className="h-5 w-5 text-purple-400" />
            <div>
              <h2 className="font-mono text-sm font-bold text-text-primary tracking-wide">
                NEXUS Intelligence Analysis
              </h2>
              <span className="text-[10px] font-mono text-text-muted">
                Evidence-grounded opportunity breakdown &amp; strategic assessment.
              </span>
            </div>
          </div>

          <Button
            variant="outline"
            size="sm"
            loading={analyzing}
            onClick={handleGenerateAnalysis}
            className="shrink-0"
          >
            <Bot className="mr-1.5 h-3.5 w-3.5 text-purple-400" />
            {analysis ? "Regenerate AI Analysis" : "Generate AI Intelligence"}
          </Button>
        </div>

        {analysisError && (
          <div className="p-3 rounded-md bg-danger/10 border border-danger/30 text-xs font-mono text-danger">
            {analysisError}
          </div>
        )}

        {!analysis && !analyzing && (
          <div className="p-8 text-center rounded-md border border-dashed border-surface-border space-y-2">
            <Bot className="h-6 w-6 text-text-muted mx-auto" />
            <p className="text-xs font-mono text-text-muted">
              Click &quot;Generate AI Intelligence&quot; to inspect deep opportunity requirements, organization overview, and personalized strategic fit.
            </p>
          </div>
        )}

        {analyzing && (
          <div className="p-8 text-center rounded-md border border-surface-border/40 space-y-2 font-mono text-xs text-text-muted">
            <RefreshCw className="h-5 w-5 animate-spin mx-auto text-purple-400" />
            <p>Synthesizing opportunity specifications and analyzing profile compatibility...</p>
          </div>
        )}

        {analysis && !analyzing && (
          <div className="space-y-4 text-xs font-mono">
            {/* Recommendation Strip */}
            <div className="flex items-center justify-between p-3 rounded-sm bg-bg-secondary border border-surface-border">
              <div className="flex items-center gap-2">
                <span className="text-text-muted">Strategic Recommendation:</span>
                {renderRecommendationBadge(analysis.recommendation)}
              </div>
              <span className="text-[10px] text-text-muted">
                Model: {analysis.modelName} ({analysis.promptVersion})
              </span>
            </div>

            {/* Analysis Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="p-4 rounded-sm bg-bg-secondary/40 border border-surface-border space-y-2">
                <span className="text-[10px] uppercase tracking-wider text-text-muted font-semibold block">
                  Opportunity Summary
                </span>
                <p className="text-xs font-sans text-text-primary leading-relaxed">
                  {analysis.summary}
                </p>
              </div>

              <div className="p-4 rounded-sm bg-bg-secondary/40 border border-surface-border space-y-2">
                <span className="text-[10px] uppercase tracking-wider text-text-muted font-semibold block">
                  Organization Overview
                </span>
                <p className="text-xs font-sans text-text-primary leading-relaxed">
                  {analysis.organizationOverview || "Information grounded in provider record."}
                </p>
              </div>

              <div className="p-4 rounded-sm bg-bg-secondary/40 border border-surface-border space-y-2">
                <span className="text-[10px] uppercase tracking-wider text-text-muted font-semibold block">
                  Technical Expectations
                </span>
                <p className="text-xs font-sans text-text-primary leading-relaxed">
                  {analysis.expectations || "Standard open engineering deliverables."}
                </p>
              </div>

              <div className="p-4 rounded-sm bg-bg-secondary/40 border border-surface-border space-y-2">
                <span className="text-[10px] uppercase tracking-wider text-text-muted font-semibold block">
                  Key Timeline &amp; Application Process
                </span>
                <p className="text-xs font-sans text-text-primary leading-relaxed">
                  {analysis.keyDates} {analysis.applicationProcess}
                </p>
              </div>
            </div>

            {/* Personalized Strategic Assessment */}
            <div className="p-4 rounded-sm bg-bg-secondary border border-brand/20 space-y-2">
              <span className="text-[10px] uppercase tracking-wider text-brand font-semibold block">
                Personalized Strategic Assessment
              </span>
              <p className="text-xs font-sans text-text-primary leading-relaxed">
                {analysis.personalizedAssessment}
              </p>

              {/* Strengths & Weaknesses tags */}
              <div className="pt-2 grid grid-cols-1 sm:grid-cols-2 gap-2 text-[11px]">
                {analysis.strengths && analysis.strengths.length > 0 && (
                  <div>
                    <span className="text-brand font-medium block text-[10px]">Identified Strengths:</span>
                    <ul className="list-disc list-inside text-text-secondary mt-1">
                      {analysis.strengths.map((s, idx) => (
                        <li key={idx}>{s}</li>
                      ))}
                    </ul>
                  </div>
                )}

                {analysis.weaknesses && analysis.weaknesses.length > 0 && (
                  <div>
                    <span className="text-amber-400 font-medium block text-[10px]">Unlisted Competencies:</span>
                    <ul className="list-disc list-inside text-text-secondary mt-1">
                      {analysis.weaknesses.map((w, idx) => (
                        <li key={idx}>{w}</li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            </div>

            <div className="text-[10px] text-text-muted/80 italic">
              Disclaimer: NEXUS Intelligence analyses are strictly evidence-grounded and synthesized from verified opportunity data. AI analysis never overrides deterministic eligibility rules.
            </div>
          </div>
        )}
      </div>

      {/* Main Grid: Details + Personal Notes */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Description & Criteria */}
        <div className="lg:col-span-2 space-y-6">
          {/* Full Description */}
          <div className="p-5 rounded-md bg-surface-card border border-surface-border space-y-3">
            <span className="text-[11px] font-mono uppercase tracking-wider text-text-secondary font-semibold">
              Program Description &amp; Scope
            </span>
            <p className="text-sm font-sans text-text-primary leading-relaxed whitespace-pre-line">
              {opportunity.description}
            </p>
          </div>

          {/* Eligibility Criteria */}
          {opportunity.eligibilityCriteria && (
            <div className="p-5 rounded-md bg-surface-card border border-surface-border space-y-3">
              <span className="text-[11px] font-mono uppercase tracking-wider text-text-secondary font-semibold">
                Eligibility Requirements
              </span>
              <p className="text-sm font-sans text-text-secondary leading-relaxed">
                {opportunity.eligibilityCriteria}
              </p>
            </div>
          )}

          {/* Required & Preferred Skills */}
          <div className="p-5 rounded-md bg-surface-card border border-surface-border space-y-4">
            <span className="text-[11px] font-mono uppercase tracking-wider text-text-secondary font-semibold">
              Target Skills Matrix
            </span>

            {opportunity.requiredSkills && opportunity.requiredSkills.length > 0 && (
              <div className="space-y-1.5">
                <span className="text-xs font-mono text-text-muted block">Required Technical Competencies:</span>
                <div className="flex flex-wrap gap-1.5">
                  {opportunity.requiredSkills.map((s) => (
                    <Badge key={s} variant="secondary">
                      {s}
                    </Badge>
                  ))}
                </div>
              </div>
            )}

            {opportunity.preferredSkills && opportunity.preferredSkills.length > 0 && (
              <div className="space-y-1.5 pt-2">
                <span className="text-xs font-mono text-text-muted block">Preferred / Bonus Tools:</span>
                <div className="flex flex-wrap gap-1.5">
                  {opportunity.preferredSkills.map((s) => (
                    <Badge key={s} variant="outline">
                      {s}
                    </Badge>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Right Column: Personal Notes & Provenance */}
        <div className="space-y-6">
          {/* Personal Preparation Notes */}
          <div className="p-5 rounded-md bg-surface-card border border-surface-border space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-mono uppercase tracking-wider text-brand font-semibold">
                Personal Notes
              </span>
              {noteSuccess && <span className="text-[10px] font-mono text-success">Saved!</span>}
            </div>

            <textarea
              rows={4}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Add your preparation checklist, target proposal ideas, or referral notes..."
              className="w-full rounded-md border border-surface-border bg-bg-secondary p-2.5 text-xs text-text-primary placeholder:text-text-muted focus:border-brand focus:outline-none font-mono"
            />

            <Button
              variant="outline"
              size="sm"
              loading={savingNote}
              onClick={handleSaveNotes}
              className="w-full text-xs"
            >
              <Save className="mr-1.5 h-3.5 w-3.5" /> Save Personal Notes
            </Button>
          </div>

          {/* Verification Provenance */}
          <div className="p-5 rounded-md bg-surface-card border border-surface-border space-y-3 text-xs font-mono text-text-muted">
            <span className="text-[11px] font-mono uppercase tracking-wider text-text-secondary font-semibold block">
              Opportunity Provenance
            </span>
            <div className="space-y-2">
              <div>
                <span className="block text-[10px] text-text-muted">Ingestion Source:</span>
                <span className="text-text-primary font-medium flex items-center gap-1 mt-0.5">
                  <ShieldCheck className="h-3.5 w-3.5 text-brand" />
                  {opportunity.sourceName || "NEXUS Verified Index"}
                </span>
              </div>
              <div>
                <span className="block text-[10px] text-text-muted">Last Verified:</span>
                <span className="text-text-secondary">
                  {new Date(opportunity.lastVerifiedAt).toLocaleString()}
                </span>
              </div>
              <div>
                <span className="block text-[10px] text-text-muted">Official Portal:</span>
                <a
                  href={opportunity.applicationUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-brand hover:underline truncate block"
                >
                  {opportunity.applicationUrl}
                </a>
              </div>
              {opportunity.canonicalUrl && opportunity.canonicalUrl !== opportunity.applicationUrl && (
                <div>
                  <span className="block text-[10px] text-text-muted">Canonical Identifier:</span>
                  <span className="text-text-muted text-[10px] truncate block font-mono">
                    {opportunity.canonicalUrl}
                  </span>
                </div>
              )}
            </div>

            <div className="pt-3 border-t border-surface-border/60 text-[11px] font-sans text-text-muted leading-normal">
              NEXUS provides opportunity discovery and strategic preparation. Applications are completed directly on the provider&apos;s official portal.
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
