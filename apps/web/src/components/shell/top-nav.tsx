"use client";

import React from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { Bell, Terminal, ShieldCheck, ChevronRight } from "lucide-react";

const TITLE_MAP: Record<string, { title: string; category: string }> = {
  "/dashboard": { title: "Command Center", category: "Core Workspace" },
  "/onboarding": { title: "Profile Onboarding", category: "Setup" },
  "/profile": { title: "Developer Profile", category: "Identity" },
  "/skills": { title: "Technical Skills Matrix", category: "Capabilities" },
  "/settings": { title: "Account & Preferences", category: "Preferences" },
};

export function TopNav() {
  const pathname = usePathname();
  const current = TITLE_MAP[pathname] || { title: "Command Center", category: "NEXUS" };

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

        {/* Notifications Placeholder */}
        <button
          className="relative p-1.5 text-text-secondary hover:text-text-primary hover:bg-surface-hover rounded-md transition-colors border border-transparent hover:border-surface-border"
          title="Notifications"
        >
          <Bell className="h-4 w-4" />
          <span className="absolute top-1 right-1 h-1.5 w-1.5 rounded-full bg-brand" />
        </button>
      </div>
    </header>
  );
}
