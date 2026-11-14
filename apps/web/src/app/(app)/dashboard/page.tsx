"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import {
  User,
  Cpu,
  Sliders,
  CheckCircle2,
  Circle,
  ArrowRight,
  Sparkles,
  Compass,
  FileCode,
  GitBranch,
  ShieldAlert,
} from "lucide-react";
import { useAuth } from "@/lib/auth-context";
import { api } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { UserProfile, UserSkill, UserPreferences, OpportunityMatch } from "@/lib/types";

export default function DashboardPage() {
  const { user } = useAuth();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [preferences, setPreferences] = useState<UserPreferences | null>(null);
  const [skills, setSkills] = useState<UserSkill[]>([]);
  const [topMatches, setTopMatches] = useState<OpportunityMatch[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchData() {
      try {
        const [p, prefs, s] = await Promise.all([
          api.profile.get().catch(() => null),
          api.preferences.get().catch(() => null),
          api.skills.getUserSkills().catch(() => []),
        ]);
        setProfile(p);
        setPreferences(prefs);
        setSkills(s);

        api.matching.getRecommendations({ size: 3 })
          .then((res) => setTopMatches(res.content || []))
          .catch(() => {});
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, []);

  // Compute Onboarding / Profile completion
  const checklist = [
    { label: "Account registered and secured", done: true, href: "#" },
    {
      label: "Basic identity and education details",
      done: Boolean(profile?.institution && profile?.degree),
      href: "/onboarding",
    },
    {
      label: `Technical skills added (${skills.length} registered)`,
      done: skills.length >= 3,
      href: "/skills",
    },
    {
      label: "Opportunity categories & weekly bandwidth configured",
      done: Boolean(preferences?.preferredCategories?.length),
      href: "/settings",
    },
    {
      label: "Complete full onboarding profile workflow",
      done: Boolean(profile?.onboardingCompleted),
      href: "/onboarding",
    },
  ];

  const completedCount = checklist.filter((item) => item.done).length;
  const progressPercent = Math.round((completedCount / checklist.length) * 100);

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      {/* Welcome Banner */}
      <div className="p-6 rounded-md bg-surface-card border border-surface-border flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <span className="text-[10px] font-mono uppercase tracking-widest text-brand font-semibold">
              Command Center
            </span>
            <Badge variant="brand">Phase 1 Active</Badge>
          </div>
          <h1 className="text-xl sm:text-2xl font-mono font-bold text-text-primary">
            Welcome back, {user?.fullName || "Developer"}
          </h1>
          <p className="text-xs font-mono text-text-muted">
            Authenticated as <span className="text-text-secondary">{user?.email}</span> · System foundation ready
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Link href="/onboarding">
            <Button variant="outline" size="sm">
              <Sliders className="mr-1.5 h-3.5 w-3.5" />
              Onboarding Wizard
            </Button>
          </Link>
          <Link href="/profile">
            <Button variant="primary" size="sm">
              <User className="mr-1.5 h-3.5 w-3.5" />
              View Profile
            </Button>
          </Link>
        </div>
      </div>

      {/* Profile Readiness & Checklist */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Readiness Meter */}
        <div className="p-5 rounded-md bg-surface-card border border-surface-border space-y-4 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-mono uppercase tracking-wider text-text-secondary font-semibold">
                Profile Readiness
              </span>
              <span className="font-mono text-xs font-bold text-brand">{progressPercent}%</span>
            </div>
            <div className="h-1.5 w-full bg-bg-secondary rounded-full overflow-hidden mt-2 border border-surface-border">
              <div
                className="h-full bg-brand transition-all duration-500 rounded-full"
                style={{ width: `${progressPercent}%` }}
              />
            </div>
            <p className="text-xs font-sans text-text-secondary mt-3">
              {progressPercent === 100
                ? "Your engineer profile is fully configured and ready for intelligence modules."
                : "Complete the setup checklist to unlock high-precision opportunity matching."}
            </p>
          </div>

          <div className="pt-4 border-t border-surface-border flex items-center justify-between text-xs font-mono">
            <span className="text-text-muted">Skills Registered</span>
            <span className="font-semibold text-text-primary">{skills.length}</span>
          </div>
        </div>

        {/* Getting Started Checklist */}
        <div className="lg:col-span-2 p-5 rounded-md bg-surface-card border border-surface-border space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-mono uppercase tracking-wider text-text-secondary font-semibold">
              Getting Started Checklist
            </span>
            <span className="text-xs font-mono text-text-muted">
              {completedCount} of {checklist.length} completed
            </span>
          </div>

          <div className="space-y-2 pt-1">
            {checklist.map((item, idx) => (
              <div
                key={idx}
                className="p-2.5 rounded-sm bg-bg-secondary border border-surface-border flex items-center justify-between font-mono text-xs"
              >
                <div className="flex items-center gap-2.5">
                  {item.done ? (
                    <CheckCircle2 className="h-4 w-4 text-brand shrink-0" />
                  ) : (
                    <Circle className="h-4 w-4 text-text-muted shrink-0" />
                  )}
                  <span className={item.done ? "text-text-primary" : "text-text-secondary"}>
                    {item.label}
                  </span>
                </div>
                {!item.done && item.href !== "#" && (
                  <Link href={item.href} className="text-brand hover:underline text-[11px] flex items-center gap-1">
                    Configure <ArrowRight className="h-3 w-3" />
                  </Link>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Top Recommendations (Phase 4 Intelligence) */}
      {topMatches.length > 0 && (
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Sparkles className="h-4 w-4 text-brand" />
              <h2 className="text-base font-mono font-bold text-text-primary">
                Top Matches For Your Profile
              </h2>
            </div>
            <Link href="/recommendations" className="text-xs font-mono text-brand hover:underline flex items-center gap-1">
              View all recommendations <ArrowRight className="h-3 w-3" />
            </Link>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {topMatches.map((m) => (
              <div
                key={m.id}
                className="p-4 rounded-md bg-surface-card border border-surface-border hover:border-brand/40 flex flex-col justify-between space-y-3 transition-colors"
              >
                <div className="space-y-2">
                  <div className="flex items-center justify-between gap-2">
                    <span className="text-[10px] font-mono uppercase tracking-wider text-text-muted truncate">
                      {m.opportunity.organization}
                    </span>
                    <span className="px-2 py-0.5 rounded-sm font-mono font-bold text-[11px] bg-brand/15 text-brand border border-brand/30">
                      {Math.round(m.overallScore)}%
                    </span>
                  </div>

                  <Link href={`/opportunities/${m.opportunity.id}`}>
                    <h3 className="font-mono text-xs font-semibold text-text-primary hover:text-brand line-clamp-1">
                      {m.opportunity.title}
                    </h3>
                  </Link>

                  <div className="flex items-center gap-1.5">
                    <Badge variant="cyan">{m.opportunity.category}</Badge>
                    {m.opportunity.remoteAvailable && <Badge variant="brand">Remote</Badge>}
                  </div>
                </div>

                <div className="pt-2 border-t border-surface-border/60 flex items-center justify-between text-[11px] font-mono text-text-muted">
                  <span>{m.eligibility.status}</span>
                  <Link href={`/opportunities/${m.opportunity.id}`} className="text-brand hover:underline">
                    Inspect →
                  </Link>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Future Intelligence Modules (Clearly marked coming soon) */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <div>
            <span className="text-[10px] font-mono uppercase tracking-widest text-brand font-semibold">
              Platform Roadmap
            </span>
            <h2 className="text-base font-mono font-bold text-text-primary">
              Upcoming Intelligence Modules
            </h2>
          </div>
          <span className="text-xs font-mono text-text-muted">Next Phases</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div className="p-4 rounded-md bg-surface-card border border-surface-border/70 space-y-3 opacity-80">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 font-mono text-xs font-semibold text-text-primary">
                <Compass className="h-4 w-4 text-cyan" />
                <span>NEXUS Scout</span>
              </div>
              <Badge variant="outline">Phase 2</Badge>
            </div>
            <p className="text-xs font-sans text-text-secondary">
              Real-time crawler and discovery pipeline for GSoC, hackathons, open-source programs, and student internships.
            </p>
          </div>

          <div className="p-4 rounded-md bg-surface-card border border-surface-border/70 space-y-3 opacity-80">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 font-mono text-xs font-semibold text-text-primary">
                <Sparkles className="h-4 w-4 text-warning" />
                <span>NEXUS Intelligence</span>
              </div>
              <Badge variant="outline">Phase 2</Badge>
            </div>
            <p className="text-xs font-sans text-text-secondary">
              Automated requirement decomposition, eligibility checks, and profile compatibility scoring.
            </p>
          </div>

          <div className="p-4 rounded-md bg-surface-card border border-surface-border/70 space-y-3 opacity-80">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 font-mono text-xs font-semibold text-text-primary">
                <FileCode className="h-4 w-4 text-brand" />
                <span>NEXUS Strategy</span>
              </div>
              <Badge variant="outline">Phase 3</Badge>
            </div>
            <p className="text-xs font-sans text-text-secondary">
              Personalized technical preparation tracks, milestone generation, and prerequisite skill mapping.
            </p>
          </div>

          <div className="p-4 rounded-md bg-surface-card border border-surface-border/70 space-y-3 opacity-80">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 font-mono text-xs font-semibold text-text-primary">
                <GitBranch className="h-4 w-4 text-cyan" />
                <span>NEXUS Execution</span>
              </div>
              <Badge variant="outline">Phase 3</Badge>
            </div>
            <p className="text-xs font-sans text-text-secondary">
              GitHub repository issue selector, open-source contribution assistance, and proposal reviewer.
            </p>
          </div>

          <div className="p-4 rounded-md bg-surface-card border border-surface-border/70 space-y-3 opacity-80 md:col-span-2">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 font-mono text-xs font-semibold text-text-primary">
                <ShieldAlert className="h-4 w-4 text-success" />
                <span>NEXUS Pulse</span>
              </div>
              <Badge variant="outline">Phase 4</Badge>
            </div>
            <p className="text-xs font-sans text-text-secondary">
              Unified telemetry timeline for application deadlines, acceptance dates, and milestone tracking.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
