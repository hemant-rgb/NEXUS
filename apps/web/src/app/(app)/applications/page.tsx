"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import {
  Briefcase,
  Plus,
  Search,
  Filter,
  Kanban,
  List as ListIcon,
  Calendar,
  Clock,
  ArrowRight,
  ChevronRight,
  FileText,
  Video,
  CheckCircle2,
  AlertTriangle,
  Sparkles,
  ExternalLink
} from "lucide-react";
import { api } from "@/lib/api";
import {
  Application,
  ApplicationAnalytics,
  ApplicationStatus,
  ApplicationPriority
} from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";

const STATUS_COLUMNS: { key: ApplicationStatus; label: string; color: string }[] = [
  { key: "INTERESTED", label: "Interested", color: "border-text-muted" },
  { key: "PREPARING", label: "Preparing", color: "border-warning" },
  { key: "READY_TO_APPLY", label: "Ready to Apply", color: "border-brand" },
  { key: "APPLIED", label: "Applied", color: "border-brand" },
  { key: "ASSESSMENT", label: "Assessment", color: "border-indigo-500" },
  { key: "INTERVIEW", label: "Interview", color: "border-purple-500" },
  { key: "OFFER_RECEIVED", label: "Offer", color: "border-emerald-500" },
  { key: "REJECTED", label: "Rejected / Withdrawn", color: "border-danger/60" },
];

export default function ApplicationsPage() {
  const [applications, setApplications] = useState<Application[]>([]);
  const [analytics, setAnalytics] = useState<ApplicationAnalytics | null>(null);
  const [loading, setLoading] = useState(true);

  const [viewMode, setViewMode] = useState<"kanban" | "list">("kanban");
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedStatus, setSelectedStatus] = useState<string>("ALL");

  const loadData = async () => {
    setLoading(true);
    try {
      const [apps, stats] = await Promise.all([
        api.applications.list(selectedStatus !== "ALL" ? selectedStatus : undefined),
        api.applications.getAnalytics(),
      ]);
      setApplications(apps);
      setAnalytics(stats);
    } catch (err) {
      console.error("Failed to load applications", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [selectedStatus]);

  const filteredApps = applications.filter((app) => {
    const q = searchQuery.toLowerCase();
    return (
      app.title.toLowerCase().includes(q) ||
      app.organization.toLowerCase().includes(q) ||
      app.applicationType.toLowerCase().includes(q)
    );
  });

  const handleQuickStatusChange = async (appId: string, nextStatus: ApplicationStatus) => {
    try {
      await api.applications.update(appId, { status: nextStatus });
      setApplications((prev) =>
        prev.map((a) => (a.id === appId ? { ...a, status: nextStatus } : a))
      );
      // Reload stats
      const stats = await api.applications.getAnalytics();
      setAnalytics(stats);
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
              NEXUS Execution Pipeline
            </span>
            <Badge variant="outline" className="font-mono text-[10px] text-text-muted">
              Phase 7
            </Badge>
          </div>
          <h1 className="text-2xl font-mono font-bold text-text-primary mt-1">
            Application Pipeline & Execution
          </h1>
          <p className="text-xs font-mono text-text-secondary mt-1 max-w-2xl">
            Track applications from initial discovery through AI preparation, tailored document submission, interview rounds, and offers.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Link href="/resumes">
            <Button variant="outline" size="sm" className="font-mono text-xs">
              <FileText className="h-3.5 w-3.5 mr-1.5" />
              Manage Resumes
            </Button>
          </Link>
          <Link href="/applications/new">
            <Button size="sm" className="font-mono text-xs">
              <Plus className="h-3.5 w-3.5 mr-1.5" />
              New Application
            </Button>
          </Link>
        </div>
      </div>

      {/* Analytics Summary Banner */}
      {analytics && (
        <div className="grid grid-cols-2 sm:grid-cols-5 gap-3">
          <div className="p-3.5 rounded-lg border border-surface-border bg-surface-card">
            <span className="text-[10px] font-mono uppercase tracking-wider text-text-muted">
              Total Applications
            </span>
            <div className="text-xl font-mono font-bold text-text-primary mt-1">
              {analytics.totalApplications}
            </div>
          </div>

          <div className="p-3.5 rounded-lg border border-surface-border bg-surface-card">
            <span className="text-[10px] font-mono uppercase tracking-wider text-text-muted">
              Active In-Flight
            </span>
            <div className="text-xl font-mono font-bold text-brand mt-1">
              {analytics.appliedCount + analytics.inProgressCount + analytics.interviewCount}
            </div>
          </div>

          <div className="p-3.5 rounded-lg border border-surface-border bg-surface-card">
            <span className="text-[10px] font-mono uppercase tracking-wider text-text-muted">
              Interview Conversion
            </span>
            <div className="text-xl font-mono font-bold text-indigo-400 mt-1">
              {analytics.interviewRate}%
            </div>
          </div>

          <div className="p-3.5 rounded-lg border border-surface-border bg-surface-card">
            <span className="text-[10px] font-mono uppercase tracking-wider text-text-muted">
              Offer Rate
            </span>
            <div className="text-xl font-mono font-bold text-emerald-400 mt-1">
              {analytics.offerRate}%
            </div>
          </div>

          <div className="p-3.5 rounded-lg border border-surface-border bg-surface-card col-span-2 sm:col-span-1">
            <span className="text-[10px] font-mono uppercase tracking-wider text-text-muted">
              Upcoming Deadlines
            </span>
            <div className="text-xl font-mono font-bold text-warning mt-1">
              {analytics.upcomingDeadlinesCount}
            </div>
          </div>
        </div>
      )}

      {/* Filter and View Toggle Controls */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 p-3 rounded-lg border border-surface-border bg-bg-secondary">
        <div className="flex-1 relative">
          <Search className="absolute left-3 top-2.5 h-3.5 w-3.5 text-text-muted" />
          <input
            type="text"
            placeholder="Search by role, company, or opportunity type..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-9 pr-3 py-1.5 bg-bg-main border border-surface-border rounded text-xs font-mono text-text-primary placeholder:text-text-muted focus:outline-none focus:border-brand"
          />
        </div>

        <div className="flex items-center gap-2">
          <select
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
            className="px-2.5 py-1.5 bg-bg-main border border-surface-border rounded text-xs font-mono text-text-secondary focus:outline-none focus:border-brand"
          >
            <option value="ALL">All Statuses</option>
            <option value="INTERESTED">Interested</option>
            <option value="PREPARING">Preparing</option>
            <option value="READY_TO_APPLY">Ready to Apply</option>
            <option value="APPLIED">Applied</option>
            <option value="ASSESSMENT">Assessment</option>
            <option value="INTERVIEW">Interview</option>
            <option value="OFFER_RECEIVED">Offer Received</option>
            <option value="REJECTED">Rejected / Withdrawn</option>
          </select>

          <div className="flex items-center border border-surface-border rounded bg-bg-main p-0.5">
            <button
              onClick={() => setViewMode("kanban")}
              className={`p-1.5 rounded text-xs transition-colors ${
                viewMode === "kanban" ? "bg-surface-hover text-brand font-bold" : "text-text-muted hover:text-text-primary"
              }`}
              title="Kanban Board View"
            >
              <Kanban className="h-3.5 w-3.5" />
            </button>
            <button
              onClick={() => setViewMode("list")}
              className={`p-1.5 rounded text-xs transition-colors ${
                viewMode === "list" ? "bg-surface-hover text-brand font-bold" : "text-text-muted hover:text-text-primary"
              }`}
              title="List View"
            >
              <ListIcon className="h-3.5 w-3.5" />
            </button>
          </div>
        </div>
      </div>

      {/* Main View */}
      {loading ? (
        <div className="p-12 text-center text-xs font-mono text-text-muted">
          Loading applications pipeline...
        </div>
      ) : filteredApps.length === 0 ? (
        <div className="p-12 rounded-lg border border-surface-border bg-surface-card text-center space-y-3">
          <Briefcase className="h-8 w-8 text-text-muted mx-auto" />
          <p className="font-mono text-xs text-text-muted">
            No applications match your filter.
          </p>
          <Link href="/applications/new">
            <Button size="sm" className="font-mono text-xs">
              <Plus className="h-3.5 w-3.5 mr-1" />
              Create First Application
            </Button>
          </Link>
        </div>
      ) : viewMode === "kanban" ? (
        /* Kanban Board View */
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 overflow-x-auto pb-4">
          {STATUS_COLUMNS.map((col) => {
            const colApps = filteredApps.filter((a) => {
              if (col.key === "REJECTED") {
                return a.status === "REJECTED" || a.status === "WITHDRAWN";
              }
              return a.status === col.key;
            });

            return (
              <div
                key={col.key}
                className="flex flex-col rounded-lg border border-surface-border bg-bg-secondary/40 min-h-[500px]"
              >
                {/* Column Header */}
                <div className={`p-3 border-b-2 border-surface-border ${col.color} flex items-center justify-between`}>
                  <div className="flex items-center gap-2">
                    <span className="font-mono text-xs font-bold text-text-primary">
                      {col.label}
                    </span>
                    <span className="px-1.5 py-0.2 rounded-full bg-surface-hover font-mono text-[10px] text-text-muted">
                      {colApps.length}
                    </span>
                  </div>
                </div>

                {/* Column Body / Cards */}
                <div className="p-2.5 flex-1 space-y-2.5 overflow-y-auto max-h-[700px]">
                  {colApps.length === 0 ? (
                    <div className="h-28 flex items-center justify-center border border-dashed border-surface-border/50 rounded-md text-[11px] font-mono text-text-muted/60">
                      Empty column
                    </div>
                  ) : (
                    colApps.map((app) => (
                      <div
                        key={app.id}
                        className="p-3 rounded border border-surface-border bg-surface-card hover:border-surface-hover transition-colors space-y-2.5 shadow-sm"
                      >
                        <div className="flex items-start justify-between gap-1.5">
                          <div>
                            <span className="font-mono text-[10px] uppercase tracking-wider text-brand font-semibold block">
                              {app.organization}
                            </span>
                            <Link
                              href={`/applications/${app.id}`}
                              className="font-mono text-xs font-bold text-text-primary hover:text-brand line-clamp-1"
                            >
                              {app.title}
                            </Link>
                          </div>
                          <Badge
                            variant={
                              app.priority === "HIGH"
                                ? "danger"
                                : app.priority === "MEDIUM"
                                ? "warning"
                                : "outline"
                            }
                            className="font-mono text-[9px] px-1.5 py-0 shrink-0"
                          >
                            {app.priority}
                          </Badge>
                        </div>

                        {/* Badges / telemetry */}
                        <div className="flex items-center gap-3 text-[11px] font-mono text-text-muted">
                          {app.deadline && (
                            <span className="flex items-center gap-1 text-text-secondary">
                              <Calendar className="h-3 w-3 text-warning" />
                              {new Date(app.deadline).toLocaleDateString(undefined, { month: "short", day: "numeric" })}
                            </span>
                          )}
                          {app.documentsCount > 0 && (
                            <span className="flex items-center gap-0.5">
                              <FileText className="h-3 w-3" /> {app.documentsCount}
                            </span>
                          )}
                          {app.interviewsCount > 0 && (
                            <span className="flex items-center gap-0.5 text-indigo-400">
                              <Video className="h-3 w-3" /> {app.interviewsCount}
                            </span>
                          )}
                        </div>

                        {/* Quick Status Advance */}
                        <div className="pt-2 border-t border-surface-border/60 flex items-center justify-between">
                          <select
                            value={app.status}
                            onChange={(e) => handleQuickStatusChange(app.id, e.target.value as ApplicationStatus)}
                            className="text-[10px] font-mono bg-bg-main border border-surface-border rounded px-1.5 py-0.5 text-text-muted focus:outline-none"
                          >
                            <option value="INTERESTED">Interested</option>
                            <option value="PREPARING">Preparing</option>
                            <option value="READY_TO_APPLY">Ready to Apply</option>
                            <option value="APPLIED">Applied</option>
                            <option value="ASSESSMENT">Assessment</option>
                            <option value="INTERVIEW">Interview</option>
                            <option value="OFFER_RECEIVED">Offer</option>
                            <option value="REJECTED">Rejected</option>
                            <option value="WITHDRAWN">Withdrawn</option>
                          </select>

                          <Link
                            href={`/applications/${app.id}`}
                            className="text-[10px] font-mono text-brand hover:underline flex items-center gap-0.5"
                          >
                            View <ChevronRight className="h-3 w-3" />
                          </Link>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        /* List View */
        <div className="rounded-lg border border-surface-border bg-surface-card overflow-hidden">
          <table className="w-full text-left font-mono text-xs">
            <thead className="bg-bg-secondary border-b border-surface-border text-text-muted uppercase text-[10px]">
              <tr>
                <th className="p-3">Organization & Role</th>
                <th className="p-3">Type</th>
                <th className="p-3">Status</th>
                <th className="p-3">Priority</th>
                <th className="p-3">Deadline</th>
                <th className="p-3">Docs / Interviews</th>
                <th className="p-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-border/60">
              {filteredApps.map((app) => (
                <tr key={app.id} className="hover:bg-surface-hover/50 transition-colors">
                  <td className="p-3">
                    <span className="text-[10px] uppercase text-brand font-semibold block">
                      {app.organization}
                    </span>
                    <Link href={`/applications/${app.id}`} className="font-bold text-text-primary hover:text-brand">
                      {app.title}
                    </Link>
                  </td>
                  <td className="p-3">
                    <Badge variant="outline" className="text-[10px]">
                      {app.applicationType}
                    </Badge>
                  </td>
                  <td className="p-3">
                    <Badge
                      variant={
                        app.status === "OFFER_RECEIVED" || app.status === "ACCEPTED"
                          ? "success"
                          : app.status === "INTERVIEW"
                          ? "secondary"
                          : app.status === "REJECTED"
                          ? "danger"
                          : "outline"
                      }
                      className="text-[10px]"
                    >
                      {app.status}
                    </Badge>
                  </td>
                  <td className="p-3">
                    <span className="text-text-secondary">{app.priority}</span>
                  </td>
                  <td className="p-3 text-text-muted">
                    {app.deadline ? new Date(app.deadline).toLocaleDateString() : "—"}
                  </td>
                  <td className="p-3 text-text-muted">
                    <div className="flex items-center gap-2">
                      <span>{app.documentsCount} docs</span>
                      <span>•</span>
                      <span>{app.interviewsCount} rounds</span>
                    </div>
                  </td>
                  <td className="p-3 text-right">
                    <Link href={`/applications/${app.id}`}>
                      <Button variant="outline" size="sm" className="font-mono text-[11px] h-7 px-2">
                        Open Workspace
                      </Button>
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
