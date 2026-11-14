"use client";

import React, { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import {
  ArrowLeft,
  Calendar,
  Clock,
  CheckCircle2,
  Circle,
  AlertTriangle,
  RotateCcw,
  Sparkles,
  ExternalLink,
  ChevronDown,
  ChevronRight,
  BookOpen,
  Plus,
  Trash2,
  Target,
  FileText,
  Save,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { api } from "@/lib/api";
import {
  Roadmap,
  RoadmapTask,
  RoadmapMilestone,
  TaskStatus,
  TaskType,
} from "@/lib/types";

export default function RoadmapDetailPage() {
  const params = useParams();
  const router = useRouter();
  const roadmapId = params.id as string;

  const [roadmap, setRoadmap] = useState<Roadmap | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);

  // Expanded task ID for drawer/details
  const [expandedTaskId, setExpandedTaskId] = useState<string | null>(null);
  const [taskNotes, setTaskNotes] = useState<{ [taskId: string]: string }>({});
  const [savingNotes, setSavingNotes] = useState<{ [taskId: string]: boolean }>({});

  // Custom resource inline form state
  const [addingResourceForTask, setAddingResourceForTask] = useState<string | null>(null);
  const [customResTitle, setCustomResTitle] = useState("");
  const [customResUrl, setCustomResUrl] = useState("");

  useEffect(() => {
    loadRoadmap();
  }, [roadmapId]);

  const loadRoadmap = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await api.roadmaps.get(roadmapId);
      setRoadmap(data);

      // Initialize notes state
      const notesMap: { [id: string]: string } = {};
      data.milestones.forEach((m: RoadmapMilestone) => {
        m.tasks.forEach((t: RoadmapTask) => {
          if (t.userNotes) {
            notesMap[t.id] = t.userNotes;
          }
        });
      });
      setTaskNotes(notesMap);
    } catch (err: any) {
      setError(err?.message || "Failed to load roadmap specification");
    } finally {
      setLoading(false);
    }
  };

  const handleToggleTaskStatus = async (task: RoadmapTask) => {
    if (!roadmap) return;
    const newStatus: TaskStatus = task.status === "COMPLETED" ? "NOT_STARTED" : "COMPLETED";

    try {
      // Optimistic update
      const updatedMilestones = roadmap.milestones.map((m) => {
        if (m.id === task.milestoneId) {
          const updatedTasks = m.tasks.map((t) =>
            t.id === task.id ? { ...t, status: newStatus } : t
          );
          return { ...m, tasks: updatedTasks };
        }
        return m;
      });
      setRoadmap({ ...roadmap, milestones: updatedMilestones });

      await api.roadmaps.updateTask(roadmap.id, task.id, { status: newStatus });
      // Refresh to get accurately recalculated milestone & overall percentages
      const refreshed = await api.roadmaps.get(roadmap.id);
      setRoadmap(refreshed);
    } catch (err: any) {
      alert("Failed to update task status: " + err.message);
      loadRoadmap();
    }
  };

  const handleSaveNotes = async (taskId: string) => {
    if (!roadmap) return;
    setSavingNotes((prev) => ({ ...prev, [taskId]: true }));
    try {
      await api.roadmaps.updateTask(roadmap.id, taskId, { userNotes: taskNotes[taskId] || "" });
    } catch (err: any) {
      alert("Failed to save notes: " + err.message);
    } finally {
      setSavingNotes((prev) => ({ ...prev, [taskId]: false }));
    }
  };

  const handleReschedule = async () => {
    if (!roadmap) return;
    if (!confirm("Reschedule all remaining uncompleted tasks starting from today?")) return;
    try {
      setActionLoading(true);
      await api.roadmaps.reschedule(roadmap.id);
      await loadRoadmap();
    } catch (err: any) {
      alert("Failed to reschedule roadmap: " + err.message);
    } finally {
      setActionLoading(false);
    }
  };

  const handleRegenerate = async () => {
    if (!roadmap) return;
    if (!confirm("Regenerate uncompleted milestones? All completed tasks and notes will be preserved.")) return;
    try {
      setActionLoading(true);
      const regenerated = await api.roadmaps.regenerate(roadmap.id);
      setRoadmap(regenerated);
    } catch (err: any) {
      alert("Failed to regenerate roadmap: " + err.message);
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!roadmap) return;
    if (!confirm("Are you sure you want to delete this roadmap? This action cannot be undone.")) return;
    try {
      await api.roadmaps.delete(roadmap.id);
      router.push("/roadmaps");
    } catch (err: any) {
      alert("Failed to delete roadmap: " + err.message);
    }
  };

  const handleAddResource = async (taskId: string) => {
    if (!customResTitle.trim() || !customResUrl.trim()) {
      alert("Please enter title and URL");
      return;
    }
    try {
      await api.roadmaps.addTaskResource(taskId, {
        customTitle: customResTitle.trim(),
        customUrl: customResUrl.trim(),
        customProvider: "User Reference",
      });
      setAddingResourceForTask(null);
      setCustomResTitle("");
      setCustomResUrl("");
      await loadRoadmap();
    } catch (err: any) {
      alert("Failed to add resource: " + err.message);
    }
  };

  const renderTaskTypeBadge = (type: TaskType) => {
    switch (type) {
      case "LEARNING":
        return <Badge variant="secondary">Learning</Badge>;
      case "PRACTICE":
        return <Badge variant="cyan">Practice</Badge>;
      case "PROJECT":
        return <Badge variant="brand">Project</Badge>;
      case "RESEARCH":
        return <Badge variant="outline">Research</Badge>;
      case "APPLICATION_PREPARATION":
        return <Badge variant="outline" className="border-purple-500/40 text-purple-300">App Prep</Badge>;
      case "ASSESSMENT":
        return <Badge variant="outline" className="border-emerald-500/40 text-emerald-400">Assessment</Badge>;
      default:
        return <Badge variant="secondary">{type}</Badge>;
    }
  };

  if (loading) {
    return (
      <div className="flex h-96 items-center justify-center font-mono text-xs text-text-muted">
        Loading preparation roadmap specification and task telemetry...
      </div>
    );
  }

  if (error || !roadmap) {
    return (
      <div className="max-w-4xl mx-auto p-6 space-y-4">
        <div className="p-4 rounded-md bg-danger/10 border border-danger/30 text-xs font-mono text-danger">
          {error || "Roadmap not found"}
        </div>
        <Link href="/roadmaps">
          <Button variant="outline" size="sm">
            <ArrowLeft className="mr-1.5 h-3.5 w-3.5" /> Back to Roadmaps
          </Button>
        </Link>
      </div>
    );
  }

  const todayStr = new Date().toISOString().split("T")[0];

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      {/* Navigation & Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <Link
          href="/roadmaps"
          className="inline-flex items-center text-xs font-mono text-text-muted hover:text-text-primary transition-colors"
        >
          <ArrowLeft className="mr-1.5 h-3.5 w-3.5" /> Back to All Roadmaps
        </Link>

        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={handleReschedule}
            disabled={actionLoading}
            title="Reschedule remaining tasks from today"
          >
            <RotateCcw className="mr-1.5 h-3.5 w-3.5 text-cyan-400" />
            Reschedule
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={handleRegenerate}
            disabled={actionLoading}
            title="Regenerate uncompleted milestones"
          >
            <Sparkles className="mr-1.5 h-3.5 w-3.5 text-brand" />
            Regenerate
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={handleDelete}
            className="text-text-muted hover:text-danger hover:border-danger/40"
          >
            <Trash2 className="h-3.5 w-3.5" />
          </Button>
        </div>
      </div>

      {/* Main Roadmap Header Card */}
      <div className="p-6 rounded-md bg-surface-card border border-surface-border space-y-5">
        <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4 pb-4 border-b border-surface-border/60">
          <div className="space-y-1.5">
            <div className="flex items-center gap-2">
              <Badge variant={roadmap.roadmapType === "OPPORTUNITY_PREPARATION" ? "brand" : "cyan"}>
                {roadmap.roadmapType === "OPPORTUNITY_PREPARATION" ? "Opportunity Preparation" : "Goal Mastery"}
              </Badge>
              <Badge variant="outline">{roadmap.status}</Badge>
            </div>
            <h1 className="text-2xl font-mono font-bold text-text-primary">
              {roadmap.title}
            </h1>
            {roadmap.targetOpportunityTitle && (
              <div className="text-xs font-mono text-brand flex items-center gap-1.5">
                <Target className="h-3.5 w-3.5" />
                <span>
                  Target Opportunity:{" "}
                  {roadmap.targetOpportunityId ? (
                    <Link
                      href={`/opportunities/${roadmap.targetOpportunityId}`}
                      className="underline hover:text-brand-hover"
                    >
                      {roadmap.targetOpportunityTitle}
                    </Link>
                  ) : (
                    roadmap.targetOpportunityTitle
                  )}
                </span>
              </div>
            )}
            {roadmap.targetGoal && !roadmap.targetOpportunityTitle && (
              <div className="text-xs font-mono text-cyan-400 flex items-center gap-1.5">
                <Target className="h-3.5 w-3.5" />
                <span>Career Mastery Goal: {roadmap.targetGoal}</span>
              </div>
            )}
          </div>

          <div className="text-right shrink-0 font-mono">
            <span className="text-[10px] uppercase text-text-muted block">Overall Progress</span>
            <span className="text-3xl font-bold text-brand">
              {Math.round(roadmap.overallProgressPercentage)}%
            </span>
          </div>
        </div>

        {/* Progress Bar */}
        <div className="space-y-1.5">
          <div className="w-full bg-surface-border h-2.5 rounded-full overflow-hidden">
            <div
              className="bg-brand h-full rounded-full transition-all duration-300"
              style={{ width: `${Math.min(100, Math.max(0, roadmap.overallProgressPercentage))}%` }}
            />
          </div>
        </div>

        {/* Telemetry Strip */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 pt-2 text-xs font-mono">
          <div>
            <span className="text-text-muted block text-[10px] uppercase">Milestones Done</span>
            <span className="font-semibold text-text-primary">
              {roadmap.completedMilestones} of {roadmap.totalMilestones} Completed
            </span>
          </div>
          <div>
            <span className="text-text-muted block text-[10px] uppercase">Tasks Done</span>
            <span className="font-semibold text-text-primary">
              {roadmap.completedTasks} of {roadmap.totalTasks} Completed
            </span>
          </div>
          <div>
            <span className="text-text-muted block text-[10px] uppercase">Pacing Budget</span>
            <span className="font-semibold text-text-primary">
              {roadmap.availableHoursPerWeek} Hours / Week
            </span>
          </div>
          <div>
            <span className="text-text-muted block text-[10px] uppercase">Target Deadline</span>
            <span className="font-semibold text-text-primary">
              {roadmap.targetCompletionDate || "Flexible"}
            </span>
          </div>
        </div>
      </div>

      {/* Milestones & Tasks Section */}
      <div className="space-y-6">
        <h2 className="text-sm font-mono font-bold uppercase tracking-wider text-text-secondary">
          Preparation Milestones &amp; Action Checklists
        </h2>

        {roadmap.milestones.map((milestone) => {
          const isMilestoneDone = milestone.status === "COMPLETED";

          return (
            <div
              key={milestone.id}
              className="p-5 rounded-md bg-surface-card border border-surface-border space-y-4"
            >
              {/* Milestone Header */}
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-surface-border/60">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-mono text-brand font-bold">
                      #{milestone.sequenceOrder}
                    </span>
                    <h3 className="font-mono text-base font-bold text-text-primary">
                      {milestone.title}
                    </h3>
                    <Badge
                      variant={isMilestoneDone ? "secondary" : "outline"}
                      className={isMilestoneDone ? "text-emerald-400" : ""}
                    >
                      {milestone.status}
                    </Badge>
                  </div>
                  {milestone.description && (
                    <p className="text-xs font-sans text-text-secondary">
                      {milestone.description}
                    </p>
                  )}
                </div>

                <div className="flex items-center gap-3 shrink-0 font-mono text-xs">
                  {milestone.targetDate && (
                    <div className="text-text-muted flex items-center gap-1">
                      <Calendar className="h-3.5 w-3.5" />
                      <span>Target: {milestone.targetDate}</span>
                    </div>
                  )}
                  <span className="px-2 py-0.5 rounded-sm bg-bg-secondary border border-surface-border font-semibold">
                    {Math.round(milestone.completionPercentage)}% Done
                  </span>
                </div>
              </div>

              {/* Tasks List */}
              <div className="space-y-2">
                {milestone.tasks.map((task) => {
                  const isTaskDone = task.status === "COMPLETED";
                  const isOverdue =
                    !isTaskDone &&
                    task.scheduledDate &&
                    task.scheduledDate < todayStr;
                  const isExpanded = expandedTaskId === task.id;

                  return (
                    <div
                      key={task.id}
                      className={`p-3.5 rounded-sm border transition-colors ${
                        isTaskDone
                          ? "bg-bg-secondary/40 border-surface-border/40"
                          : isOverdue
                          ? "bg-danger/5 border-danger/30"
                          : "bg-bg-secondary border-surface-border"
                      }`}
                    >
                      {/* Task Row */}
                      <div className="flex items-start justify-between gap-3">
                        <div className="flex items-start gap-3">
                          <button
                            type="button"
                            onClick={() => handleToggleTaskStatus(task)}
                            className="mt-0.5 text-text-muted hover:text-brand transition-colors focus:outline-none"
                            title={isTaskDone ? "Mark Incomplete" : "Mark Complete"}
                          >
                            {isTaskDone ? (
                              <CheckCircle2 className="h-4 w-4 text-brand" />
                            ) : (
                              <Circle className="h-4 w-4 text-text-muted hover:text-brand" />
                            )}
                          </button>

                          <div className="space-y-1">
                            <span
                              className={`font-mono text-xs font-medium block ${
                                isTaskDone
                                  ? "line-through text-text-muted"
                                  : "text-text-primary"
                              }`}
                            >
                              {task.title}
                            </span>

                            <div className="flex flex-wrap items-center gap-2 text-[10px] font-mono">
                              {renderTaskTypeBadge(task.taskType)}
                              <Badge variant="outline" className="text-[9px]">
                                {task.priority} Priority
                              </Badge>
                              <span className="text-text-muted flex items-center gap-1">
                                <Clock className="h-3 w-3" />
                                {task.estimatedHours}h
                              </span>
                              {task.scheduledDate && (
                                <span
                                  className={`flex items-center gap-1 ${
                                    isOverdue
                                      ? "text-danger font-bold"
                                      : "text-text-muted"
                                  }`}
                                >
                                  {isOverdue && <AlertTriangle className="h-3 w-3" />}
                                  <span>Sched: {task.scheduledDate}</span>
                                </span>
                              )}
                            </div>
                          </div>
                        </div>

                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => setExpandedTaskId(isExpanded ? null : task.id)}
                          className="h-7 text-xs text-text-muted hover:text-text-primary"
                        >
                          {isExpanded ? (
                            <ChevronDown className="h-3.5 w-3.5" />
                          ) : (
                            <ChevronRight className="h-3.5 w-3.5" />
                          )}
                        </Button>
                      </div>

                      {/* Expanded Task Details */}
                      {isExpanded && (
                        <div className="mt-3 pt-3 border-t border-surface-border/60 space-y-3 pl-7 text-xs font-mono">
                          {task.description && (
                            <div>
                              <span className="text-[10px] uppercase text-text-muted block">
                                Objective &amp; Scope:
                              </span>
                              <p className="text-xs font-sans text-text-secondary leading-relaxed">
                                {task.description}
                              </p>
                            </div>
                          )}

                          {task.completionCriteria && (
                            <div className="p-2.5 rounded-sm bg-bg-main border border-brand/20 space-y-1">
                              <span className="text-[10px] uppercase tracking-wider text-brand font-semibold block flex items-center gap-1">
                                <CheckCircle2 className="h-3 w-3" /> Concrete Completion Criteria:
                              </span>
                              <p className="text-xs font-sans text-text-primary leading-relaxed">
                                {task.completionCriteria}
                              </p>
                            </div>
                          )}

                          {/* Attached Learning Resources */}
                          <div className="space-y-1.5 pt-1">
                            <div className="flex items-center justify-between">
                              <span className="text-[10px] uppercase text-text-muted block flex items-center gap-1">
                                <BookOpen className="h-3 w-3" /> Learning Resources:
                              </span>
                              <button
                                type="button"
                                onClick={() =>
                                  setAddingResourceForTask(
                                    addingResourceForTask === task.id ? null : task.id
                                  )
                                }
                                className="text-[10px] text-brand hover:underline inline-flex items-center gap-1"
                              >
                                <Plus className="h-3 w-3" /> Add Link
                              </button>
                            </div>

                            {task.resources && task.resources.length > 0 ? (
                              <div className="space-y-1">
                                {task.resources.map((res) => (
                                  <div
                                    key={res.id}
                                    className="p-2 rounded-sm bg-bg-main border border-surface-border flex items-center justify-between text-xs"
                                  >
                                    <div className="space-y-0.5">
                                      <span className="font-semibold text-text-primary block">
                                        {res.title}
                                      </span>
                                      <span className="text-[10px] text-text-muted block">
                                        Provider: {res.provider} · Status: {res.verificationStatus}
                                      </span>
                                    </div>
                                    <a
                                      href={res.url}
                                      target="_blank"
                                      rel="noopener noreferrer"
                                      className="text-brand hover:underline text-xs inline-flex items-center gap-1"
                                    >
                                      Open <ExternalLink className="h-3 w-3" />
                                    </a>
                                  </div>
                                ))}
                              </div>
                            ) : (
                              <span className="text-[11px] text-text-muted italic block">
                                No specific resources attached yet.
                              </span>
                            )}

                            {/* Inline Add Resource Form */}
                            {addingResourceForTask === task.id && (
                              <div className="p-3 rounded-sm bg-bg-main border border-brand/30 space-y-2 mt-2">
                                <span className="text-[10px] uppercase font-semibold text-text-primary block">
                                  Attach Custom Link
                                </span>
                                <Input
                                  placeholder="Resource title (e.g. Official Documentation)"
                                  value={customResTitle}
                                  onChange={(e) => setCustomResTitle(e.target.value)}
                                  className="h-7 text-xs font-mono"
                                />
                                <Input
                                  placeholder="https://..."
                                  value={customResUrl}
                                  onChange={(e) => setCustomResUrl(e.target.value)}
                                  className="h-7 text-xs font-mono"
                                />
                                <div className="flex items-center justify-end gap-2">
                                  <Button
                                    variant="ghost"
                                    size="sm"
                                    className="h-6 text-xs"
                                    onClick={() => setAddingResourceForTask(null)}
                                  >
                                    Cancel
                                  </Button>
                                  <Button
                                    variant="primary"
                                    size="sm"
                                    className="h-6 text-xs"
                                    onClick={() => handleAddResource(task.id)}
                                  >
                                    Attach
                                  </Button>
                                </div>
                              </div>
                            )}
                          </div>

                          {/* Personal Developer Notes */}
                          <div className="space-y-1.5 pt-1">
                            <span className="text-[10px] uppercase text-text-muted block flex items-center gap-1">
                              <FileText className="h-3 w-3" /> Developer Notes &amp; Checklist:
                            </span>
                            <textarea
                              value={taskNotes[task.id] || ""}
                              onChange={(e) =>
                                setTaskNotes({ ...taskNotes, [task.id]: e.target.value })
                              }
                              placeholder="Record implementation findings, prototype links, or PR branches..."
                              rows={2}
                              className="w-full rounded-sm border border-surface-border bg-bg-main p-2 text-xs font-mono text-text-primary focus:outline-none focus:ring-1 focus:ring-brand"
                            />
                            <div className="flex justify-end">
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => handleSaveNotes(task.id)}
                                loading={savingNotes[task.id]}
                                className="h-6 text-[11px]"
                              >
                                <Save className="mr-1 h-3 w-3" /> Save Notes
                              </Button>
                            </div>
                          </div>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
