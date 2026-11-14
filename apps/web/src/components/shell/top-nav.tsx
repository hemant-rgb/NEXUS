"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { Bell, Terminal, ShieldCheck, ChevronRight } from "lucide-react";
import { api } from "@/lib/api";

const TITLE_MAP: Record<string, { title: string; category: string }> = {
  "/dashboard": { title: "Command Center", category: "Core Workspace" },
  "/opportunities": { title: "Opportunity Discovery", category: "Scout Engine" },
  "/recommendations": { title: "Personalized Recommendations", category: "Intelligence" },
  "/saved": { title: "Saved Opportunities", category: "Bookmarks" },
  "/applications": { title: "Applications Pipeline", category: "Execution" },
  "/github": { title: "GitHub OS & Contributions", category: "Execution" },
  "/roadmaps": { title: "Preparation Strategy Roadmaps", category: "Strategy" },
  "/agents": { title: "Autonomous Agent Workspace", category: "Orchestration" },
  "/activity": { title: "NEXUS Pulse & Monitoring", category: "Observability" },
  "/onboarding": { title: "Profile Onboarding", category: "Setup" },
  "/profile": { title: "Developer Profile", category: "Identity" },
  "/skills": { title: "Technical Skills Matrix", category: "Capabilities" },
  "/settings": { title: "Account & Preferences", category: "Preferences" },
};

export function TopNav() {
  const pathname = usePathname();
  const [unreadCount, setUnreadCount] = useState<number>(0);

  useEffect(() => {
    let mounted = true;
    const fetchUnread = async () => {
      try {
        const res = await api.pulse.getUnreadCount();
        if (mounted && res) {
          setUnreadCount(res.unreadCount);
        }
      } catch {
        // Suppress on initial/unauthenticated states
      }
    };
    fetchUnread();
    const interval = setInterval(fetchUnread, 15000);
    return () => {
      mounted = false;
      clearInterval(interval);
    };
  }, [pathname]);

  let current = TITLE_MAP[pathname];
  if (!current && pathname.startsWith("/opportunities/")) {
    current = { title: "Opportunity Specification", category: "Scout Engine" };
  }
  if (!current && pathname.startsWith("/applications/")) {
    current = { title: "Application Execution Workspace", category: "Execution" };
  }
  if (!current && pathname.startsWith("/roadmaps/")) {
    current = { title: "Roadmap Plan & Tasks", category: "Strategy" };
  }
  if (!current) {
    current = { title: "Command Center", category: "NEXUS" };
  }

  return (
    <header className="h-14 border-b border-surface-border bg-bg-secondary px-6 flex items-center justify-between select-none">
      {/* Breadcrumb & Title */}
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-1.5 text-xs font-mono text-text-muted">
          <span>NEXUS</span>
          <ChevronRight className="h-3 w-3" />
          <span>{current.category}</span>
          <ChevronRight className="h-3 w-3" />
        </div>
        <h1 className="text-sm font-mono font-semibold text-text-primary tracking-wide">
          {current.title}
        </h1>
      </div>

      {/* Right Actions */}
      <div className="flex items-center gap-4">
        {/* API Telemetry Indicator */}
        <div className="hidden sm:flex items-center gap-2 px-2.5 py-1 rounded-sm bg-bg-main border border-surface-border text-[11px] font-mono text-text-secondary">
          <Terminal className="h-3.5 w-3.5 text-brand" />
          <span>REST API v1</span>
          <span className="h-1.5 w-1.5 rounded-full bg-brand" />
        </div>

        {/* Security Indicator */}
        <div className="hidden md:flex items-center gap-1.5 text-[11px] font-mono text-text-muted">
          <ShieldCheck className="h-3.5 w-3.5 text-success" />
          <span>Encrypted Session</span>
        </div>

        {/* Notifications / Pulse Button */}
        <Link
          href="/activity"
          className="relative p-1.5 text-text-secondary hover:text-text-primary hover:bg-surface-hover rounded-md transition-colors border border-transparent hover:border-surface-border flex items-center justify-center"
          title={unreadCount > 0 ? `${unreadCount} unread alerts` : "Pulse Notifications"}
        >
          <Bell className="h-4 w-4" />
          {unreadCount > 0 ? (
            <span className="absolute -top-1 -right-1 min-w-[16px] h-4 px-1 rounded-full bg-brand text-black font-mono font-bold text-[9px] flex items-center justify-center">
              {unreadCount > 9 ? "9+" : unreadCount}
            </span>
          ) : (
            <span className="absolute top-1 right-1 h-1.5 w-1.5 rounded-full bg-brand/60" />
          )}
        </Link>
      </div>
    </header>
  );
}
