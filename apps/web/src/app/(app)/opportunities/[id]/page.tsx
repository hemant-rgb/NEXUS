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
  CheckCircle,
  Clock,
  Save,
  AlertCircle,
  Building,
} from "lucide-react";
import { api } from "@/lib/api";
import { Opportunity } from "@/lib/types";
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
    } catch (err: any) {
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

  if (loading) {
    return (
      <div className="flex h-96 items-center justify-center font-mono text-xs text-text-muted">
        Loading opportunity specification...
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
      <div>
        <Link href="/opportunities" className="inline-flex items-center text-xs font-mono text-text-muted hover:text-text-primary transition-colors">
          <ArrowLeft className="mr-1.5 h-3.5 w-3.5" /> Back to Opportunity Feed
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
            <span className="text-text-muted block text-[10px] uppercase tracking-wider">Status &amp; Verification</span>
            <span className="font-semibold text-success flex items-center gap-1">
              <ShieldCheck className="h-3 w-3" /> {opportunity.status}
            </span>
          </div>
        </div>
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
