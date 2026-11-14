"use client";

import React from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutDashboard,
  Compass,
  Sparkles,
  Bookmark,
  Briefcase,
  Map,
  GitBranch,
  Activity,
  Settings,
  User as UserIcon,
  Cpu,
  LogOut,
} from "lucide-react";
import { useAuth } from "@/lib/auth-context";
import { cn } from "@/lib/utils";

interface NavItem {
  name: string;
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  status?: "active" | "coming-soon";
}

const NAV_ITEMS: NavItem[] = [
  { name: "Overview", href: "/dashboard", icon: LayoutDashboard, status: "active" },
  { name: "Discover", href: "/opportunities", icon: Compass, status: "active" },
  { name: "Recommended", href: "/recommendations", icon: Sparkles, status: "active" },
  { name: "Saved", href: "/saved", icon: Bookmark, status: "active" },
  { name: "Profile", href: "/profile", icon: UserIcon, status: "active" },
  { name: "Skills", href: "/skills", icon: Cpu, status: "active" },
  { name: "Settings", href: "/settings", icon: Settings, status: "active" },
  { name: "My Applications", href: "#", icon: Briefcase, status: "coming-soon" },
  { name: "Roadmaps", href: "/roadmaps", icon: Map, status: "active" },
  { name: "GitHub", href: "#", icon: GitBranch, status: "coming-soon" },
  { name: "Activity", href: "#", icon: Activity, status: "coming-soon" },
];

export function Sidebar() {
  const pathname = usePathname();
  const { user, logout } = useAuth();

  return (
    <aside className="w-64 border-r border-surface-border bg-bg-secondary flex flex-col h-screen select-none shrink-0">
      {/* Brand Header */}
      <div className="h-14 border-b border-surface-border px-5 flex items-center justify-between">
        <Link href="/dashboard" className="flex items-center gap-2.5 group">
          <div className="h-7 w-7 rounded-sm bg-brand flex items-center justify-center text-black font-mono font-bold text-sm shadow-sm group-hover:bg-brand-hover transition-colors">
            N
          </div>
          <div className="flex flex-col">
            <span className="font-mono font-bold text-sm tracking-wider text-text-primary">
              NEXUS
            </span>
            <span className="font-mono text-[9px] uppercase tracking-widest text-text-muted">
              v0.1.0-alpha
            </span>
          </div>
        </Link>
        <span className="h-2 w-2 rounded-full bg-brand animate-pulse" title="System Online" />
      </div>

      {/* Navigation */}
      <div className="flex-1 overflow-y-auto px-3 py-4 space-y-6">
        <div>
          <div className="px-3 pb-2 text-[10px] font-mono uppercase tracking-widest text-text-muted font-semibold">
            Core Workspace
          </div>
          <nav className="space-y-1">
            {NAV_ITEMS.filter((i) => i.status === "active").map((item) => {
              const Icon = item.icon;
              const isActive = pathname === item.href;
              return (
                <Link
                  key={item.name}
                  href={item.href}
                  className={cn(
                    "flex items-center justify-between px-3 py-2 rounded-md font-mono text-xs transition-colors",
                    isActive
                      ? "bg-surface-hover text-brand border border-surface-border font-medium"
                      : "text-text-secondary hover:text-text-primary hover:bg-surface-hover"
                  )}
                >
                  <div className="flex items-center gap-2.5">
                    <Icon className={cn("h-4 w-4", isActive ? "text-brand" : "text-text-muted")} />
                    <span>{item.name}</span>
                  </div>
                  {isActive && <div className="h-1.5 w-1.5 rounded-full bg-brand" />}
                </Link>
              );
            })}
          </nav>
        </div>

        <div>
          <div className="px-3 pb-2 text-[10px] font-mono uppercase tracking-widest text-text-muted font-semibold">
            Future Intelligence
          </div>
          <nav className="space-y-1">
            {NAV_ITEMS.filter((i) => i.status === "coming-soon").map((item) => {
              const Icon = item.icon;
              return (
                <div
                  key={item.name}
                  className="flex items-center justify-between px-3 py-2 rounded-md font-mono text-xs text-text-muted cursor-not-allowed hover:bg-surface-card/40 opacity-70"
                  title="Coming in Phase 2/3"
                >
                  <div className="flex items-center gap-2.5">
                    <Icon className="h-4 w-4 text-text-muted" />
                    <span>{item.name}</span>
                  </div>
                  <span className="text-[9px] uppercase tracking-wider px-1.5 py-0.5 rounded-sm bg-surface-border/40 text-text-muted font-mono border border-surface-border/60">
                    SOON
                  </span>
                </div>
              );
            })}
          </nav>
        </div>
      </div>

      {/* User Footer */}
      <div className="p-3 border-t border-surface-border bg-bg-main/50">
        <div className="p-2 rounded-md bg-surface-card border border-surface-border flex items-center justify-between">
          <div className="flex items-center gap-2.5 min-w-0">
            <div className="h-7 w-7 rounded-sm bg-surface-hover border border-surface-border flex items-center justify-center font-mono text-xs font-semibold text-brand shrink-0">
              {user?.fullName?.charAt(0)?.toUpperCase() || "U"}
            </div>
            <div className="truncate">
              <p className="text-xs font-mono font-medium text-text-primary truncate">
                {user?.fullName || "Developer"}
              </p>
              <p className="text-[10px] font-mono text-text-muted truncate">
                {user?.email || "dev@nexus.io"}
              </p>
            </div>
          </div>
          <button
            onClick={() => logout()}
            className="p-1.5 text-text-muted hover:text-danger hover:bg-surface-hover rounded-sm transition-colors"
            title="Log out"
          >
            <LogOut className="h-4 w-4" />
          </button>
        </div>
      </div>
    </aside>
  );
}
