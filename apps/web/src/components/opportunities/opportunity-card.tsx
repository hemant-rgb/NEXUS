"use client";

import React, { useState } from "react";
import Link from "next/link";
import {
  Bookmark,
  BookmarkCheck,
  Calendar,
  Globe,
  MapPin,
  ArrowUpRight,
  ShieldCheck,
} from "lucide-react";
import { Opportunity } from "@/lib/types";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";

interface OpportunityCardProps {
  opportunity: Opportunity;
  onToggleSave?: (opportunityId: string, currentSaved: boolean) => Promise<void>;
  showSaveButton?: boolean;
}

export function OpportunityCard({
  opportunity,
  onToggleSave,
  showSaveButton = true,
}: OpportunityCardProps) {
  const [isSaved, setIsSaved] = useState<boolean>(opportunity.isSaved || false);
  const [saving, setSaving] = useState(false);

  const handleSaveClick = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!onToggleSave || saving) return;

    setSaving(true);
    const nextSavedState = !isSaved;
    setIsSaved(nextSavedState); // optimistic update
    try {
      await onToggleSave(opportunity.id, isSaved);
    } catch {
      setIsSaved(isSaved); // revert on error
    } finally {
      setSaving(false);
    }
  };

  const isDeadlineSoon =
    opportunity.deadline &&
    new Date(opportunity.deadline).getTime() - Date.now() < 14 * 24 * 60 * 60 * 1000;

  return (
    <div className="p-5 rounded-md bg-surface-card border border-surface-border hover:border-surface-border/80 hover:bg-surface-card/90 transition-all flex flex-col justify-between space-y-4 group">
      {/* Top Metadata */}
      <div className="space-y-2.5">
        <div className="flex items-start justify-between gap-3">
          <div className="space-y-1 min-w-0">
            <span className="text-[11px] font-mono uppercase tracking-wider text-text-muted block truncate">
              {opportunity.organization}
            </span>
            <Link href={`/opportunities/${opportunity.id}`} className="block">
              <h3 className="font-mono text-sm font-semibold text-text-primary group-hover:text-brand transition-colors line-clamp-1">
                {opportunity.title}
              </h3>
            </Link>
          </div>

          {showSaveButton && (
            <button
              onClick={handleSaveClick}
              disabled={saving}
              title={isSaved ? "Remove from saved" : "Save opportunity"}
              className={`p-1.5 rounded-sm border transition-colors shrink-0 ${
                isSaved
                  ? "bg-brand/15 text-brand border-brand/40"
                  : "bg-bg-secondary text-text-muted hover:text-text-primary border-surface-border"
              }`}
            >
              {isSaved ? <BookmarkCheck className="h-4 w-4" /> : <Bookmark className="h-4 w-4" />}
            </button>
          )}
        </div>

        {/* Badges: Category & Remote/Location */}
        <div className="flex flex-wrap items-center gap-1.5">
          <Badge variant="cyan">{opportunity.category}</Badge>
          {opportunity.sourceName && (
            <Badge variant="outline" className="border-brand/30 text-brand bg-brand/5">
              <ShieldCheck className="h-2.5 w-2.5 mr-0.5 inline" /> {opportunity.sourceName}
            </Badge>
          )}
          {opportunity.remoteAvailable ? (
            <Badge variant="brand">Remote</Badge>
          ) : (
            opportunity.location && (
              <Badge variant="outline">
                <MapPin className="h-2.5 w-2.5 mr-0.5 inline" /> {opportunity.location}
              </Badge>
            )
          )}
          {opportunity.deadlineType === "ROLLING" && (
            <Badge variant="secondary">Rolling</Badge>
          )}
        </div>

        {/* Description snippet */}
        <p className="text-xs font-sans text-text-secondary line-clamp-2 leading-relaxed">
          {opportunity.description}
        </p>
      </div>

      {/* Skills & Footer */}
      <div className="space-y-3 pt-3 border-t border-surface-border/60">
        {/* Required skills */}
        {opportunity.requiredSkills && opportunity.requiredSkills.length > 0 && (
          <div className="flex flex-wrap gap-1 items-center">
            {opportunity.requiredSkills.slice(0, 3).map((skill) => (
              <span
                key={skill}
                className="px-2 py-0.5 rounded-sm bg-bg-secondary border border-surface-border text-[10px] font-mono text-text-secondary"
              >
                {skill}
              </span>
            ))}
            {opportunity.requiredSkills.length > 3 && (
              <span className="text-[10px] font-mono text-text-muted">
                +{opportunity.requiredSkills.length - 3}
              </span>
            )}
          </div>
        )}

        {/* Footer info: Deadline & Actions */}
        <div className="flex items-center justify-between text-xs font-mono pt-1">
          <div className="flex flex-col gap-0.5 text-text-muted">
            <div className="flex items-center gap-1.5">
              <Calendar className="h-3.5 w-3.5" />
              <span className={isDeadlineSoon ? "text-warning font-medium" : ""}>
                {opportunity.deadline ? opportunity.deadline : "No deadline"}
              </span>
            </div>
            {opportunity.lastVerifiedAt && (
              <span className="text-[10px] text-text-muted/80">
                Verified: {new Date(opportunity.lastVerifiedAt).toLocaleDateString()}
              </span>
            )}
          </div>

          <Link href={`/opportunities/${opportunity.id}`}>
            <Button variant="outline" size="sm" className="h-7 px-2.5 text-[11px]">
              Inspect <ArrowUpRight className="ml-1 h-3 w-3" />
            </Button>
          </Link>
        </div>
      </div>
    </div>
  );
}
