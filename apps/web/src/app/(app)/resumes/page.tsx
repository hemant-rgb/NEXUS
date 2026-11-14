"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import {
  FileText,
  Plus,
  Sparkles,
  Star,
  CheckCircle2,
  Trash2,
  ExternalLink,
  Copy,
  Check,
  Briefcase,
  GraduationCap,
  Code,
  FolderGit2,
  Layers
} from "lucide-react";
import { api } from "@/lib/api";
import { Resume, Opportunity } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";

export default function ResumesPage() {
  const [resumes, setResumes] = useState<Resume[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedResume, setSelectedResume] = useState<Resume | null>(null);

  // New Resume Modal
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [title, setTitle] = useState("");
  const [version, setVersion] = useState("v1.0");
  const [summary, setSummary] = useState("");
  const [isPrimary, setIsPrimary] = useState(false);
  const [creating, setCreating] = useState(false);

  // Tailor Modal
  const [showTailorModal, setShowTailorModal] = useState(false);
  const [tailorTargetOppId, setTailorTargetOppId] = useState("");
  const [tailorInstructions, setTailorInstructions] = useState("");
  const [tailoring, setTailoring] = useState(false);
  const [opportunities, setOpportunities] = useState<Opportunity[]>([]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [list, oppsPage] = await Promise.all([
        api.applications.listResumes(),
        api.opportunities.list({ page: 0, size: 20 }),
      ]);
      setResumes(list);
      setOpportunities(oppsPage.content || []);
      if (list.length > 0 && !selectedResume) {
        setSelectedResume(list[0]);
      }
    } catch (err) {
      console.error("Failed to load resumes", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleCreateResume = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) return;
    setCreating(true);
    try {
      const created = await api.applications.createResume({
        title: title.trim(),
        versionName: version.trim() || "v1.0",
        professionalSummary: summary.trim(),
        isPrimary,
        educationJson: JSON.stringify([{ school: "University", degree: "B.S. Computer Science", year: "2026" }]),
        skillsJson: JSON.stringify([{ category: "Languages", skills: ["Java", "TypeScript", "Python"] }]),
        experienceJson: JSON.stringify([]),
        projectsJson: JSON.stringify([]),
        contributionsJson: JSON.stringify([]),
      });
      setResumes((prev) => [created, ...prev]);
      setSelectedResume(created);
      setShowCreateModal(false);
      setTitle("");
      setSummary("");
    } catch (err) {
      console.error(err);
    } finally {
      setCreating(false);
    }
  };

  const handleMakePrimary = async (resume: Resume) => {
    try {
      const updated = await api.applications.updateResume(resume.id, { isPrimary: true });
      setResumes((prev) =>
        prev.map((r) => ({ ...r, isPrimary: r.id === updated.id }))
      );
      if (selectedResume?.id === resume.id) {
        setSelectedResume(updated);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleOpenTailorModal = (resume: Resume) => {
    setSelectedResume(resume);
    if (opportunities.length > 0) {
      setTailorTargetOppId(opportunities[0].id);
    }
    setShowTailorModal(true);
  };

  const handleTailorResume = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedResume || !tailorTargetOppId) return;
    setTailoring(true);
    try {
      const tailored = await api.applications.tailorResume(selectedResume.id, {
        targetOpportunityId: tailorTargetOppId,
        instructions: tailorInstructions.trim() || undefined,
      });
      setResumes((prev) => [tailored, ...prev]);
      setSelectedResume(tailored);
      setShowTailorModal(false);
      setTailorInstructions("");
    } catch (err) {
      console.error(err);
    } finally {
      setTailoring(false);
    }
  };

  const handleDeleteResume = async (id: string) => {
    if (!confirm("Delete this resume version?")) return;
    try {
      await api.applications.deleteResume(id);
      const remaining = resumes.filter((r) => r.id !== id);
      setResumes(remaining);
      setSelectedResume(remaining.length > 0 ? remaining[0] : null);
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
              Structured Developer Profiles
            </span>
            <Badge variant="outline" className="font-mono text-[10px] text-text-muted">
              Phase 7
            </Badge>
          </div>
          <h1 className="text-2xl font-mono font-bold text-text-primary mt-1">
            Resume Management & AI Tailoring
          </h1>
          <p className="text-xs font-mono text-text-secondary mt-1 max-w-2xl">
            Maintain structured developer resume versions, highlight verified open-source contributions, and generate tailored variants without fabricating unverified facts.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Link href="/applications">
            <Button variant="outline" size="sm" className="font-mono text-xs">
              <Briefcase className="h-3.5 w-3.5 mr-1.5" />
              Applications
            </Button>
          </Link>
          <Button size="sm" onClick={() => setShowCreateModal(true)} className="font-mono text-xs">
            <Plus className="h-3.5 w-3.5 mr-1.5" />
            Create Resume Version
          </Button>
        </div>
      </div>

      {loading ? (
        <div className="p-12 text-center text-xs font-mono text-text-muted">
          Loading resume specifications...
        </div>
      ) : resumes.length === 0 ? (
        <div className="p-12 rounded-lg border border-surface-border bg-surface-card text-center space-y-3">
          <FileText className="h-8 w-8 text-text-muted mx-auto" />
          <p className="font-mono text-xs text-text-muted">
            No resumes configured yet. Create a baseline resume version from your developer profile.
          </p>
          <Button size="sm" onClick={() => setShowCreateModal(true)} className="font-mono text-xs">
            <Plus className="h-3.5 w-3.5 mr-1" />
            Create Baseline Resume
          </Button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Resume Version Cards */}
          <div className="space-y-3">
            <h3 className="font-mono text-xs uppercase tracking-wider text-text-muted font-semibold">
              Resume Versions ({resumes.length})
            </h3>
            {resumes.map((resume) => {
              const isSelected = selectedResume?.id === resume.id;
              return (
                <div
                  key={resume.id}
                  onClick={() => setSelectedResume(resume)}
                  className={`p-3.5 rounded-lg border font-mono text-xs cursor-pointer transition-colors space-y-2 ${
                    isSelected
                      ? "bg-surface-hover/80 border-brand"
                      : "bg-surface-card border-surface-border hover:border-surface-hover"
                  }`}
                >
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <div className="flex items-center gap-1.5">
                        <span className="font-bold text-text-primary">{resume.title}</span>
                        {resume.isPrimary && (
                          <Badge variant="success" className="text-[9px] px-1.5 py-0">
                            Primary
                          </Badge>
                        )}
                      </div>
                      <span className="text-[10px] text-text-muted">
                        Version {resume.versionName} • Updated {new Date(resume.updatedAt).toLocaleDateString()}
                      </span>
                    </div>

                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleDeleteResume(resume.id);
                      }}
                      className="text-text-muted hover:text-danger p-1"
                      title="Delete version"
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                    </button>
                  </div>

                  {resume.targetOpportunityTitle && (
                    <div className="text-[10px] text-brand bg-brand/10 px-2 py-0.5 rounded border border-brand/30">
                      Tailored for: {resume.targetOpportunityTitle}
                    </div>
                  )}

                  <div className="flex items-center gap-2 pt-1">
                    {!resume.isPrimary && (
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          handleMakePrimary(resume);
                        }}
                        className="text-[10px] text-text-muted hover:text-text-primary underline"
                      >
                        Set as Primary
                      </button>
                    )}
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleOpenTailorModal(resume);
                      }}
                      className="text-[10px] text-brand hover:underline flex items-center gap-1 ml-auto"
                    >
                      <Sparkles className="h-3 w-3" /> AI Tailor
                    </button>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Selected Resume Detailed Preview */}
          <div className="md:col-span-2 p-5 rounded-lg border border-surface-border bg-surface-card space-y-5 font-mono text-xs">
            {selectedResume ? (
              <>
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-surface-border pb-4">
                  <div>
                    <div className="flex items-center gap-2">
                      <h2 className="text-base font-bold text-text-primary">
                        {selectedResume.title}
                      </h2>
                      <span className="text-xs text-text-muted">
                        ({selectedResume.versionName})
                      </span>
                    </div>
                    {selectedResume.targetOpportunityTitle && (
                      <span className="text-xs text-brand">
                        Tailored for {selectedResume.targetOpportunityTitle}
                      </span>
                    )}
                  </div>

                  <Button
                    size="sm"
                    onClick={() => handleOpenTailorModal(selectedResume)}
                    className="font-mono text-xs"
                  >
                    <Sparkles className="h-3.5 w-3.5 mr-1" />
                    Tailor for Opportunity
                  </Button>
                </div>

                {/* Professional Summary */}
                <div className="space-y-1.5">
                  <span className="text-[11px] uppercase tracking-wider text-text-muted font-bold block flex items-center gap-1.5">
                    <FileText className="h-3.5 w-3.5 text-brand" /> Professional Summary
                  </span>
                  <div className="p-3 rounded bg-bg-main border border-surface-border text-text-primary whitespace-pre-wrap leading-relaxed">
                    {selectedResume.professionalSummary || "No professional summary provided."}
                  </div>
                </div>

                {/* Structured Sections Preview */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="space-y-1.5">
                    <span className="text-[11px] uppercase tracking-wider text-text-muted font-bold block flex items-center gap-1.5">
                      <GraduationCap className="h-3.5 w-3.5 text-indigo-400" /> Education
                    </span>
                    <div className="p-2.5 rounded bg-bg-main border border-surface-border text-text-secondary text-[11px] overflow-x-auto">
                      {selectedResume.educationJson || "[]"}
                    </div>
                  </div>

                  <div className="space-y-1.5">
                    <span className="text-[11px] uppercase tracking-wider text-text-muted font-bold block flex items-center gap-1.5">
                      <Code className="h-3.5 w-3.5 text-emerald-400" /> Verified Skills
                    </span>
                    <div className="p-2.5 rounded bg-bg-main border border-surface-border text-text-secondary text-[11px] overflow-x-auto">
                      {selectedResume.skillsJson || "[]"}
                    </div>
                  </div>
                </div>

                {/* Contributions */}
                <div className="space-y-1.5">
                  <span className="text-[11px] uppercase tracking-wider text-text-muted font-bold block flex items-center gap-1.5">
                    <FolderGit2 className="h-3.5 w-3.5 text-brand" /> Verified Open Source Contributions
                  </span>
                  <div className="p-2.5 rounded bg-bg-main border border-surface-border text-text-secondary text-[11px] overflow-x-auto">
                    {selectedResume.contributionsJson || "[]"}
                  </div>
                </div>
              </>
            ) : (
              <div className="p-12 text-center text-text-muted">
                Select a resume version to inspect
              </div>
            )}
          </div>
        </div>
      )}

      {/* Modal: Create Resume */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="w-full max-w-lg p-5 rounded-lg border border-surface-border bg-surface-card space-y-4">
            <div className="border-b border-surface-border pb-3">
              <h3 className="font-mono text-sm font-bold text-text-primary flex items-center gap-2">
                <Plus className="h-4 w-4 text-brand" /> Create Resume Version
              </h3>
            </div>

            <form onSubmit={handleCreateResume} className="space-y-3 font-mono text-xs">
              <div>
                <label className="block text-text-muted mb-1">Resume Title *</label>
                <Input
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="e.g. Full-Stack Systems Resume"
                  required
                  className="font-mono text-xs bg-bg-main"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-text-muted mb-1">Version Tag</label>
                  <Input
                    value={version}
                    onChange={(e) => setVersion(e.target.value)}
                    placeholder="v1.0"
                    className="font-mono text-xs bg-bg-main"
                  />
                </div>

                <div className="flex items-center pt-5">
                  <label className="flex items-center gap-2 text-text-secondary cursor-pointer">
                    <input
                      type="checkbox"
                      checked={isPrimary}
                      onChange={(e) => setIsPrimary(e.target.checked)}
                      className="rounded border-surface-border text-brand focus:ring-0"
                    />
                    Mark as Primary Resume
                  </label>
                </div>
              </div>

              <div>
                <label className="block text-text-muted mb-1">Professional Summary</label>
                <textarea
                  rows={3}
                  value={summary}
                  onChange={(e) => setSummary(e.target.value)}
                  placeholder="Summarize your engineering background, key technical specializations..."
                  className="w-full p-2 bg-bg-main border border-surface-border rounded text-xs font-mono text-text-primary focus:outline-none focus:border-brand"
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-2 border-t border-surface-border">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setShowCreateModal(false)}
                  className="font-mono text-xs"
                >
                  Cancel
                </Button>
                <Button type="submit" disabled={creating} className="font-mono text-xs">
                  {creating ? "Creating..." : "Save Resume"}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: AI Tailor Resume */}
      {showTailorModal && selectedResume && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="w-full max-w-lg p-5 rounded-lg border border-surface-border bg-surface-card space-y-4">
            <div className="border-b border-surface-border pb-3">
              <h3 className="font-mono text-sm font-bold text-text-primary flex items-center gap-2">
                <Sparkles className="h-4 w-4 text-brand" /> Tailor Resume for Opportunity
              </h3>
              <p className="text-xs font-mono text-text-muted mt-1">
                Optimizes summary and highlights relevant skills from `{selectedResume.title}` without fabricating unverified claims.
              </p>
            </div>

            <form onSubmit={handleTailorResume} className="space-y-3 font-mono text-xs">
              <div>
                <label className="block text-text-muted mb-1">Target Opportunity *</label>
                <select
                  value={tailorTargetOppId}
                  onChange={(e) => setTailorTargetOppId(e.target.value)}
                  className="w-full p-2 bg-bg-main border border-surface-border rounded text-xs font-mono text-text-primary focus:outline-none focus:border-brand"
                  required
                >
                  {opportunities.map((opp) => (
                    <option key={opp.id} value={opp.id}>
                      {opp.organization} — {opp.title} ({opp.category})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-text-muted mb-1">Tailoring Emphasis (Optional)</label>
                <textarea
                  rows={2}
                  value={tailorInstructions}
                  onChange={(e) => setTailorInstructions(e.target.value)}
                  placeholder="e.g. Focus on distributed concurrency and test-driven architecture..."
                  className="w-full p-2 bg-bg-main border border-surface-border rounded text-xs font-mono text-text-primary focus:outline-none focus:border-brand"
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-2 border-t border-surface-border">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setShowTailorModal(false)}
                  className="font-mono text-xs"
                >
                  Cancel
                </Button>
                <Button type="submit" disabled={tailoring} className="font-mono text-xs">
                  {tailoring ? "Tailoring..." : "Generate Tailored Version"}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
