"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import {
  Bookmark,
  Trash2,
  ExternalLink,
  Edit2,
  Check,
  X,
  Calendar,
  Layers,
  ArrowUpRight,
  AlertCircle,
} from "lucide-react";
import { api } from "@/lib/api";
import { SavedOpportunity } from "@/lib/types";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";

export default function SavedOpportunitiesPage() {
  const [savedList, setSavedList] = useState<SavedOpportunity[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Note inline editing
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editedNote, setEditedNote] = useState("");
  const [savingNote, setSavingNote] = useState(false);

  const fetchSaved = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.opportunities.saved();
      setSavedList(data);
    } catch (err: any) {
      setError(err?.message || "Failed to load saved opportunities.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSaved();
  }, []);

  const handleRemove = async (opportunityId: string) => {
    try {
      await api.opportunities.removeSaved(opportunityId);
      setSavedList((prev) => prev.filter((s) => s.opportunityId !== opportunityId));
    } catch {
      alert("Failed to remove saved opportunity");
    }
  };

  const startEditNote = (s: SavedOpportunity) => {
    setEditingId(s.opportunityId);
    setEditedNote(s.notes || "");
  };

  const saveNote = async (opportunityId: string) => {
    setSavingNote(true);
    try {
      const updated = await api.opportunities.updateNotes(opportunityId, editedNote);
      setSavedList((prev) =>
        prev.map((s) => (s.opportunityId === opportunityId ? { ...s, notes: updated.notes } : s))
      );
      setEditingId(null);
    } catch {
      alert("Failed to update notes");
    } finally {
      setSavingNote(false);
    }
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div>
          <span className="text-[10px] font-mono uppercase tracking-widest text-brand font-semibold">
            Bookmarks &amp; Tracking
          </span>
          <h1 className="text-xl font-mono font-bold text-text-primary mt-0.5">
            Saved Opportunities
          </h1>
          <p className="text-xs font-mono text-text-muted">
            Track and annotate your shortlisted opportunities and deadlines
          </p>
        </div>

        <div className="text-xs font-mono text-text-secondary bg-surface-card px-3 py-1.5 rounded-md border border-surface-border">
          <span className="font-semibold text-text-primary">{savedList.length}</span> opportunities saved
        </div>
      </div>

      {error && (
        <div className="p-4 rounded-md bg-danger/10 border border-danger/30 text-xs font-mono text-danger flex items-center gap-2">
          <AlertCircle className="h-4 w-4" />
          <span>{error}</span>
        </div>
      )}

      {loading ? (
        <div className="space-y-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <div
              key={i}
              className="p-5 rounded-md bg-surface-card border border-surface-border animate-pulse h-28"
            />
          ))}
        </div>
      ) : savedList.length === 0 ? (
        <div className="p-12 text-center rounded-md border border-dashed border-surface-border bg-surface-card/40 space-y-3">
          <Bookmark className="h-8 w-8 text-text-muted mx-auto" />
          <h3 className="font-mono text-sm font-semibold text-text-primary">
            No saved opportunities yet
          </h3>
          <p className="text-xs font-sans text-text-muted max-w-sm mx-auto">
            Explore the discovery catalog and bookmark opportunities you wish to target.
          </p>
          <Link href="/opportunities">
            <Button variant="primary" size="sm" className="mt-2">
              Browse Discovery Feed
            </Button>
          </Link>
        </div>
      ) : (
        <div className="space-y-3">
          {savedList.map((item) => {
            const opp = item.opportunity;
            const isEditingThis = editingId === item.opportunityId;

            return (
              <div
                key={item.id}
                className="p-5 rounded-md bg-surface-card border border-surface-border space-y-3"
              >
                <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="text-[11px] font-mono uppercase text-text-muted">
                        {opp.organization}
                      </span>
                      <span className="text-text-muted">·</span>
                      <Badge variant="cyan">{opp.category}</Badge>
                      {opp.remoteAvailable && <Badge variant="brand">Remote</Badge>}
                    </div>
                    <Link href={`/opportunities/${opp.id}`}>
                      <h3 className="text-base font-mono font-semibold text-text-primary hover:text-brand transition-colors">
                        {opp.title}
                      </h3>
                    </Link>
                  </div>

                  <div className="flex items-center gap-2 shrink-0">
                    <Link href={`/opportunities/${opp.id}`}>
                      <Button variant="outline" size="sm" className="h-8 text-xs">
                        Details <ArrowUpRight className="ml-1 h-3 w-3" />
                      </Button>
                    </Link>
                    <a href={opp.applicationUrl} target="_blank" rel="noopener noreferrer">
                      <Button variant="primary" size="sm" className="h-8 text-xs">
                        Apply <ExternalLink className="ml-1 h-3 w-3" />
                      </Button>
                    </a>
                    <button
                      onClick={() => handleRemove(item.opportunityId)}
                      className="p-2 text-text-muted hover:text-danger hover:bg-surface-hover rounded-md transition-colors"
                      title="Remove from saved"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                </div>

                {/* Deadlines and Details */}
                <div className="flex items-center gap-4 text-xs font-mono text-text-muted pt-1">
                  <span className="flex items-center gap-1.5">
                    <Calendar className="h-3.5 w-3.5" /> Deadline:{" "}
                    <span className="text-text-primary font-medium">
                      {opp.deadline || "Rolling"}
                    </span>
                  </span>
                  <span>·</span>
                  <span>Saved on {new Date(item.savedAt).toLocaleDateString()}</span>
                </div>

                {/* Personal Notes Box */}
                <div className="p-3 rounded-md bg-bg-secondary border border-surface-border space-y-1.5">
                  <div className="flex items-center justify-between">
                    <span className="text-[10px] font-mono uppercase tracking-wider text-text-muted font-semibold">
                      Personal Preparation Notes
                    </span>
                    {!isEditingThis ? (
                      <button
                        onClick={() => startEditNote(item)}
                        className="text-[11px] font-mono text-brand hover:underline flex items-center gap-1"
                      >
                        <Edit2 className="h-3 w-3" /> Edit Note
                      </button>
                    ) : (
                      <div className="flex items-center gap-1.5">
                        <button
                          onClick={() => saveNote(item.opportunityId)}
                          disabled={savingNote}
                          className="text-[11px] font-mono text-success hover:underline flex items-center gap-0.5"
                        >
                          <Check className="h-3 w-3" /> Save
                        </button>
                        <button
                          onClick={() => setEditingId(null)}
                          className="text-[11px] font-mono text-text-muted hover:underline flex items-center gap-0.5"
                        >
                          <X className="h-3 w-3" /> Cancel
                        </button>
                      </div>
                    )}
                  </div>

                  {isEditingThis ? (
                    <textarea
                      rows={2}
                      value={editedNote}
                      onChange={(e) => setEditedNote(e.target.value)}
                      placeholder="Add personal notes or strategy..."
                      className="w-full rounded border border-surface-border bg-surface-card p-2 text-xs font-mono text-text-primary focus:border-brand focus:outline-none"
                    />
                  ) : (
                    <p className="text-xs font-mono text-text-secondary italic">
                      {item.notes ? item.notes : "No personal notes added. Click edit to record application checklist or dates."}
                    </p>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
