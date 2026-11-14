"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import {
  Bell,
  Activity,
  Calendar,
  Settings,
  RefreshCw,
  CheckCheck,
  Check,
  Archive,
  ArrowUpRight,
  Clock,
  ShieldAlert,
  AlertTriangle,
  Info,
  Sliders,
  Sparkles,
} from "lucide-react";
import { api } from "@/lib/api";
import { Notification, NotificationPreference, ActivityItem, NotificationCategory } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";

export default function ActivityPulsePage() {
  const [activeTab, setActiveTab] = useState<"notifications" | "timeline" | "preferences">("notifications");
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [timeline, setTimeline] = useState<ActivityItem[]>([]);
  const [preferences, setPreferences] = useState<NotificationPreference | null>(null);
  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [categoryFilter, setCategoryFilter] = useState<string>("ALL");
  const [statusFilter, setStatusFilter] = useState<string>("ALL");
  const [loading, setLoading] = useState<boolean>(true);
  const [scanning, setScanning] = useState<boolean>(false);
  const [savingPrefs, setSavingPrefs] = useState<boolean>(false);
  const [message, setMessage] = useState<string | null>(null);

  const fetchPulseData = async () => {
    setLoading(true);
    try {
      const [notifsRes, unreadRes, timelineRes, prefsRes] = await Promise.all([
        api.pulse.getNotifications({
          category: categoryFilter !== "ALL" ? categoryFilter : undefined,
          status: statusFilter !== "ALL" ? statusFilter : undefined,
        }),
        api.pulse.getUnreadCount(),
        api.pulse.getActivity(30),
        api.pulse.getPreferences(),
      ]);

      setNotifications(notifsRes?.content || []);
      setUnreadCount(unreadRes?.unreadCount || 0);
      setTimeline(timelineRes || []);
      setPreferences(prefsRes);
    } catch (err: unknown) {
      console.error("Failed to load pulse data", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPulseData();
  }, [categoryFilter, statusFilter]);

  const handleMarkAsRead = async (id: string) => {
    try {
      await api.pulse.markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, status: "READ" as const } : n))
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch (err) {
      console.error(err);
    }
  };

  const handleArchive = async (id: string) => {
    try {
      await api.pulse.archive(id);
      setNotifications((prev) => prev.filter((n) => n.id !== id));
    } catch (err) {
      console.error(err);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await api.pulse.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, status: "READ" as const })));
      setUnreadCount(0);
      setMessage("All alerts marked as read");
      setTimeout(() => setMessage(null), 3000);
    } catch (err) {
      console.error(err);
    }
  };

  const handleTriggerMonitoring = async () => {
    setScanning(true);
    try {
      const res = await api.pulse.triggerMonitoring();
      setMessage(
        `Pulse cycle completed: ${res.opportunityDeadlinesDetected || 0} opportunities, ${res.applicationDeadlinesDetected || 0} applications, ${res.roadmapAlertsDetected || 0} roadmap tasks scanned.`
      );
      await fetchPulseData();
      setTimeout(() => setMessage(null), 4000);
    } catch (err) {
      console.error(err);
    } finally {
      setScanning(false);
    }
  };

  const handleSavePreferences = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!preferences) return;
    setSavingPrefs(true);
    try {
      const updated = await api.pulse.updatePreferences(preferences);
      setPreferences(updated);
      setMessage("Notification preferences updated successfully");
      setTimeout(() => setMessage(null), 3000);
    } catch (err) {
      console.error(err);
    } finally {
      setSavingPrefs(false);
    }
  };

  const getPriorityBadge = (priority: string) => {
    switch (priority) {
      case "CRITICAL":
        return <Badge variant="danger" className="text-[10px]">CRITICAL</Badge>;
      case "HIGH":
        return <Badge variant="warning" className="text-[10px]">HIGH</Badge>;
      case "NORMAL":
        return <Badge variant="neutral" className="text-[10px]">NORMAL</Badge>;
      default:
        return <Badge variant="neutral" className="text-[10px]">LOW</Badge>;
    }
  };

  const getCategoryIcon = (category: NotificationCategory) => {
    switch (category) {
      case "OPPORTUNITY":
        return <Calendar className="h-4 w-4 text-brand" />;
      case "APPLICATION":
        return <Activity className="h-4 w-4 text-blue-400" />;
      case "ROADMAP":
        return <Clock className="h-4 w-4 text-amber-400" />;
      case "GITHUB":
        return <Info className="h-4 w-4 text-purple-400" />;
      case "AGENT":
        return <Sparkles className="h-4 w-4 text-emerald-400" />;
      default:
        return <Bell className="h-4 w-4 text-text-muted" />;
    }
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-surface-border pb-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-mono font-bold text-text-primary tracking-wide">
              NEXUS Pulse
            </h1>
            <span className="h-2 w-2 rounded-full bg-brand animate-ping" />
            {unreadCount > 0 && (
              <Badge variant="brand" className="text-xs">
                {unreadCount} Unread
              </Badge>
            )}
          </div>
          <p className="text-xs font-mono text-text-secondary mt-1">
            Intelligent monitoring, real-time alerts, deadline telemetry, and background event orchestration
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            size="sm"
            onClick={handleTriggerMonitoring}
            disabled={scanning}
            className="flex items-center gap-2"
          >
            <RefreshCw className={`h-3.5 w-3.5 ${scanning ? "animate-spin text-brand" : ""}`} />
            <span>{scanning ? "Scanning..." : "Scan Monitoring"}</span>
          </Button>

          {unreadCount > 0 && (
            <Button
              variant="outline"
              size="sm"
              onClick={handleMarkAllAsRead}
              className="flex items-center gap-1.5"
            >
              <CheckCheck className="h-3.5 w-3.5 text-success" />
              <span>Mark All Read</span>
            </Button>
          )}
        </div>
      </div>

      {message && (
        <div className="p-3 bg-brand/10 border border-brand/30 rounded-md text-xs font-mono text-brand flex items-center justify-between">
          <span>{message}</span>
          <button onClick={() => setMessage(null)} className="hover:text-white">✕</button>
        </div>
      )}

      {/* Tabs */}
      <div className="flex items-center gap-2 border-b border-surface-border pb-2 text-xs font-mono">
        <button
          onClick={() => setActiveTab("notifications")}
          className={`px-3 py-1.5 rounded-md flex items-center gap-2 transition-colors ${
            activeTab === "notifications"
              ? "bg-surface-hover text-brand border border-surface-border font-medium"
              : "text-text-secondary hover:text-text-primary hover:bg-surface-hover"
          }`}
        >
          <Bell className="h-3.5 w-3.5" />
          <span>Alerts & Notifications ({notifications.length})</span>
        </button>

        <button
          onClick={() => setActiveTab("timeline")}
          className={`px-3 py-1.5 rounded-md flex items-center gap-2 transition-colors ${
            activeTab === "timeline"
              ? "bg-surface-hover text-brand border border-surface-border font-medium"
              : "text-text-secondary hover:text-text-primary hover:bg-surface-hover"
          }`}
        >
          <Activity className="h-3.5 w-3.5" />
          <span>Telemetry Stream ({timeline.length})</span>
        </button>

        <button
          onClick={() => setActiveTab("preferences")}
          className={`px-3 py-1.5 rounded-md flex items-center gap-2 transition-colors ${
            activeTab === "preferences"
              ? "bg-surface-hover text-brand border border-surface-border font-medium"
              : "text-text-secondary hover:text-text-primary hover:bg-surface-hover"
          }`}
        >
          <Sliders className="h-3.5 w-3.5" />
          <span>Notification Preferences</span>
        </button>
      </div>

      {/* TAB 1: NOTIFICATIONS */}
      {activeTab === "notifications" && (
        <div className="space-y-4">
          {/* Filter Bar */}
          <div className="flex flex-wrap items-center justify-between gap-3 p-3 bg-surface-card border border-surface-border rounded-lg text-xs font-mono">
            <div className="flex items-center gap-2">
              <span className="text-text-muted">Category:</span>
              {(["ALL", "OPPORTUNITY", "APPLICATION", "ROADMAP", "GITHUB", "AGENT"] as const).map((cat) => (
                <button
                  key={cat}
                  onClick={() => setCategoryFilter(cat)}
                  className={`px-2 py-1 rounded text-[11px] ${
                    categoryFilter === cat
                      ? "bg-brand text-black font-semibold"
                      : "bg-surface-hover text-text-secondary hover:text-text-primary"
                  }`}
                >
                  {cat}
                </button>
              ))}
            </div>

            <div className="flex items-center gap-2">
              <span className="text-text-muted">Status:</span>
              {(["ALL", "UNREAD"] as const).map((st) => (
                <button
                  key={st}
                  onClick={() => setStatusFilter(st)}
                  className={`px-2 py-1 rounded text-[11px] ${
                    statusFilter === st
                      ? "bg-surface-hover text-brand border border-surface-border font-semibold"
                      : "text-text-muted hover:text-text-primary"
                  }`}
                >
                  {st}
                </button>
              ))}
            </div>
          </div>

          {/* List */}
          {loading ? (
            <div className="p-8 text-center text-xs font-mono text-text-muted">
              Loading notifications from NEXUS Pulse engine...
            </div>
          ) : notifications.length === 0 ? (
            <div className="p-12 text-center border border-dashed border-surface-border rounded-lg bg-surface-card/40 space-y-2">
              <Bell className="h-8 w-8 text-text-muted mx-auto" />
              <p className="text-sm font-mono font-medium text-text-primary">No notifications found</p>
              <p className="text-xs font-mono text-text-muted">
                Your workspace is currently quiet. Trigger a monitoring scan or bookmark opportunities to generate alerts.
              </p>
              <Button
                variant="outline"
                size="sm"
                onClick={handleTriggerMonitoring}
                className="mt-3"
              >
                Trigger Pulse Scan
              </Button>
            </div>
          ) : (
            <div className="space-y-2.5">
              {notifications.map((notif) => (
                <div
                  key={notif.id}
                  className={`p-4 rounded-lg border transition-all flex flex-col sm:flex-row sm:items-start justify-between gap-4 ${
                    notif.status === "UNREAD"
                      ? "bg-surface-card border-brand/40 shadow-sm"
                      : "bg-surface-card/40 border-surface-border text-text-secondary"
                  }`}
                >
                  <div className="flex items-start gap-3 min-w-0">
                    <div className="p-2 rounded-md bg-surface-hover border border-surface-border shrink-0 mt-0.5">
                      {getCategoryIcon(notif.category)}
                    </div>
                    <div className="space-y-1 min-w-0">
                      <div className="flex flex-wrap items-center gap-2">
                        <span className="text-xs font-mono font-bold text-text-primary truncate">
                          {notif.title}
                        </span>
                        {getPriorityBadge(notif.priority)}
                        <span className="text-[10px] font-mono text-text-muted uppercase">
                          {notif.category}
                        </span>
                        {notif.status === "UNREAD" && (
                          <span className="h-1.5 w-1.5 rounded-full bg-brand" />
                        )}
                      </div>
                      <p className="text-xs font-mono text-text-secondary leading-relaxed">
                        {notif.message}
                      </p>
                      <div className="flex items-center gap-4 text-[10px] font-mono text-text-muted pt-1">
                        <span>{new Date(notif.createdAt).toLocaleString()}</span>
                        {notif.actionUrl && (
                          <Link
                            href={notif.actionUrl}
                            className="text-brand hover:underline flex items-center gap-1"
                          >
                            <span>Open Related Entity</span>
                            <ArrowUpRight className="h-3 w-3" />
                          </Link>
                        )}
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-1.5 shrink-0 self-end sm:self-start">
                    {notif.status === "UNREAD" && (
                      <button
                        onClick={() => handleMarkAsRead(notif.id)}
                        className="p-1.5 rounded text-text-muted hover:text-brand hover:bg-surface-hover"
                        title="Mark as read"
                      >
                        <Check className="h-4 w-4" />
                      </button>
                    )}
                    <button
                      onClick={() => handleArchive(notif.id)}
                      className="p-1.5 rounded text-text-muted hover:text-danger hover:bg-surface-hover"
                      title="Archive"
                    >
                      <Archive className="h-4 w-4" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* TAB 2: TELEMETRY STREAM */}
      {activeTab === "timeline" && (
        <div className="space-y-3">
          <div className="p-3 bg-surface-card border border-surface-border rounded-lg text-xs font-mono text-text-secondary">
            Continuous event stream capturing state transitions, opportunity dead-letter alerts, and background job telemetry.
          </div>
          {timeline.length === 0 ? (
            <div className="p-8 text-center text-xs font-mono text-text-muted">
              No telemetry events recorded yet.
            </div>
          ) : (
            <div className="relative pl-6 space-y-4 before:absolute before:left-2 before:top-2 before:bottom-2 before:w-[1px] before:bg-surface-border">
              {timeline.map((item) => (
                <div key={item.id} className="relative flex items-start gap-3">
                  <span className="absolute -left-6 top-1 h-3 w-3 rounded-full bg-surface-hover border-2 border-brand" />
                  <div className="p-3 bg-surface-card border border-surface-border rounded-md flex-1 text-xs font-mono space-y-1">
                    <div className="flex items-center justify-between gap-2">
                      <span className="font-bold text-text-primary">{item.title}</span>
                      <span className="text-[10px] text-text-muted">
                        {new Date(item.timestamp).toLocaleTimeString()}
                      </span>
                    </div>
                    <p className="text-text-secondary text-[11px]">{item.description}</p>
                    <div className="flex items-center gap-2 pt-1 text-[10px] text-text-muted">
                      <span className="uppercase">{item.category}</span>
                      <span>•</span>
                      <span>{item.type}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* TAB 3: NOTIFICATION PREFERENCES */}
      {activeTab === "preferences" && preferences && (
        <form onSubmit={handleSavePreferences} className="max-w-2xl bg-surface-card border border-surface-border rounded-lg p-6 space-y-6 text-xs font-mono">
          <div>
            <h2 className="text-sm font-bold text-text-primary">Delivery Channels</h2>
            <p className="text-text-muted text-[11px]">Control where and how alerts are routed</p>
            <div className="mt-3 space-y-2">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={preferences.inAppEnabled}
                  onChange={(e) => setPreferences({ ...preferences, inAppEnabled: e.target.checked })}
                  className="rounded border-surface-border bg-bg-main text-brand"
                />
                <span className="text-text-primary">In-App Notification Center (Enabled by default)</span>
              </label>

              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={preferences.emailEnabled}
                  onChange={(e) => setPreferences({ ...preferences, emailEnabled: e.target.checked })}
                  className="rounded border-surface-border bg-bg-main text-brand"
                />
                <span className="text-text-primary">Email Notifications (Configurable via SMTP / Mock Provider)</span>
              </label>
            </div>
          </div>

          <div className="border-t border-surface-border pt-4">
            <h2 className="text-sm font-bold text-text-primary">Monitored Categories</h2>
            <p className="text-text-muted text-[11px]">Select which subsystems publish alerts</p>
            <div className="mt-3 grid grid-cols-1 sm:grid-cols-2 gap-2">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={preferences.opportunityAlerts}
                  onChange={(e) => setPreferences({ ...preferences, opportunityAlerts: e.target.checked })}
                  className="rounded border-surface-border bg-bg-main text-brand"
                />
                <span>Opportunity Deadlines</span>
              </label>

              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={preferences.applicationAlerts}
                  onChange={(e) => setPreferences({ ...preferences, applicationAlerts: e.target.checked })}
                  className="rounded border-surface-border bg-bg-main text-brand"
                />
                <span>Application & Interview Reminders</span>
              </label>

              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={preferences.roadmapAlerts}
                  onChange={(e) => setPreferences({ ...preferences, roadmapAlerts: e.target.checked })}
                  className="rounded border-surface-border bg-bg-main text-brand"
                />
                <span>Roadmap Task Deadlines</span>
              </label>

              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={preferences.githubAlerts}
                  onChange={(e) => setPreferences({ ...preferences, githubAlerts: e.target.checked })}
                  className="rounded border-surface-border bg-bg-main text-brand"
                />
                <span>GitHub Issue & OS Activity</span>
              </label>

              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={preferences.agentAlerts}
                  onChange={(e) => setPreferences({ ...preferences, agentAlerts: e.target.checked })}
                  className="rounded border-surface-border bg-bg-main text-brand"
                />
                <span>Autonomous Agent Approvals</span>
              </label>
            </div>
          </div>

          <div className="border-t border-surface-border pt-4 space-y-4">
            <h2 className="text-sm font-bold text-text-primary">Quiet Hours & Timezone</h2>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div>
                <label className="text-text-muted text-[10px] block mb-1">Timezone</label>
                <input
                  type="text"
                  value={preferences.timezone}
                  onChange={(e) => setPreferences({ ...preferences, timezone: e.target.value })}
                  className="w-full bg-bg-main border border-surface-border rounded px-2.5 py-1.5 text-xs text-text-primary"
                  placeholder="UTC or America/New_York"
                />
              </div>

              <div>
                <label className="text-text-muted text-[10px] block mb-1">Quiet Hours Start</label>
                <input
                  type="text"
                  value={preferences.quietHoursStart}
                  onChange={(e) => setPreferences({ ...preferences, quietHoursStart: e.target.value })}
                  className="w-full bg-bg-main border border-surface-border rounded px-2.5 py-1.5 text-xs text-text-primary"
                  placeholder="22:00"
                />
              </div>

              <div>
                <label className="text-text-muted text-[10px] block mb-1">Quiet Hours End</label>
                <input
                  type="text"
                  value={preferences.quietHoursEnd}
                  onChange={(e) => setPreferences({ ...preferences, quietHoursEnd: e.target.value })}
                  className="w-full bg-bg-main border border-surface-border rounded px-2.5 py-1.5 text-xs text-text-primary"
                  placeholder="08:00"
                />
              </div>
            </div>

            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={preferences.quietHoursEnabled}
                onChange={(e) => setPreferences({ ...preferences, quietHoursEnabled: e.target.checked })}
                className="rounded border-surface-border bg-bg-main text-brand"
              />
              <span className="text-text-primary">Enable quiet hours (Suppresses external email alerts)</span>
            </label>
          </div>

          <div className="pt-2">
            <Button type="submit" disabled={savingPrefs} className="w-full sm:w-auto">
              {savingPrefs ? "Saving..." : "Save Pulse Preferences"}
            </Button>
          </div>
        </form>
      )}
    </div>
  );
}
