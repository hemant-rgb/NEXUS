"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { User, Mail, Globe, MapPin, Calendar, GitBranch, Edit3, ShieldCheck } from "lucide-react";
import { useAuth } from "@/lib/auth-context";
import { api } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { UserProfile, UserSkill } from "@/lib/types";

export default function ProfilePage() {
  const { user } = useAuth();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [skills, setSkills] = useState<UserSkill[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadData() {
      try {
        const [p, s] = await Promise.all([
          api.profile.get().catch(() => null),
          api.skills.getUserSkills().catch(() => []),
        ]);
        setProfile(p);
        setSkills(s);
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, []);

  if (loading) {
    return (
      <div className="flex h-96 items-center justify-center font-mono text-xs text-text-muted">
        Loading developer profile...
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      {/* Profile Header */}
      <div className="p-6 rounded-md bg-surface-card border border-surface-border flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <div className="h-16 w-16 rounded-md bg-surface-hover border border-surface-border flex items-center justify-center font-mono text-2xl font-bold text-brand">
            {user?.fullName?.charAt(0)?.toUpperCase() || "D"}
          </div>
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-mono font-bold text-text-primary">
                {user?.fullName || "Developer Profile"}
              </h1>
              <Badge variant="brand">{profile?.experienceLevel || "Developer"}</Badge>
            </div>
            <div className="flex flex-wrap items-center gap-3 text-xs font-mono text-text-muted">
              <span className="flex items-center gap-1">
                <Mail className="h-3.5 w-3.5" /> {user?.email}
              </span>
              {profile?.institution && (
                <span className="flex items-center gap-1">
                  <MapPin className="h-3.5 w-3.5" /> {profile.institution} ({profile.countryCode})
                </span>
              )}
              {profile?.githubUsername && (
                <span className="flex items-center gap-1 text-cyan">
                  <GitBranch className="h-3.5 w-3.5" /> @{profile.githubUsername}
                </span>
              )}
            </div>
          </div>
        </div>

        <Link href="/onboarding">
          <Button variant="outline" size="sm">
            <Edit3 className="mr-1.5 h-3.5 w-3.5" /> Edit Profile
          </Button>
        </Link>
      </div>

      {/* Profile Details Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Left Column: Academic & Bio */}
        <div className="md:col-span-2 space-y-6">
          <div className="p-5 rounded-md bg-surface-card border border-surface-border space-y-4">
            <span className="text-[11px] font-mono uppercase tracking-wider text-text-secondary font-semibold">
              Biography &amp; Objectives
            </span>
            <p className="text-sm font-sans text-text-secondary leading-relaxed">
              {profile?.bio || "No developer biography added yet. Update your profile in onboarding."}
            </p>
          </div>

          <div className="p-5 rounded-md bg-surface-card border border-surface-border space-y-4">
            <span className="text-[11px] font-mono uppercase tracking-wider text-text-secondary font-semibold">
              Academic Background
            </span>
            <div className="grid grid-cols-2 gap-4 text-xs font-mono">
              <div>
                <span className="text-text-muted block">Degree &amp; Major</span>
                <span className="text-text-primary font-medium">
                  {profile?.degree || "Not specified"} in {profile?.fieldOfStudy || "CS"}
                </span>
              </div>
              <div>
                <span className="text-text-muted block">Graduation Year</span>
                <span className="text-text-primary font-medium">{profile?.graduationYear || "-"}</span>
              </div>
              <div>
                <span className="text-text-muted block">Education Tier</span>
                <span className="text-text-primary font-medium">{profile?.educationLevel || "-"}</span>
              </div>
              <div>
                <span className="text-text-muted block">Timezone</span>
                <span className="text-text-primary font-medium">{profile?.timezone || "UTC"}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Right Column: Skills Matrix */}
        <div className="space-y-6">
          <div className="p-5 rounded-md bg-surface-card border border-surface-border space-y-4">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-mono uppercase tracking-wider text-text-secondary font-semibold">
                Skills ({skills.length})
              </span>
              <Link href="/skills" className="text-xs font-mono text-brand hover:underline">
                Manage
              </Link>
            </div>

            {skills.length === 0 ? (
              <p className="text-xs font-mono text-text-muted">No skills added yet.</p>
            ) : (
              <div className="flex flex-wrap gap-2">
                {skills.map((s) => (
                  <div
                    key={s.id}
                    className="p-2 rounded-sm bg-bg-secondary border border-surface-border w-full flex items-center justify-between text-xs font-mono"
                  >
                    <span className="text-text-primary font-medium">{s.skillName}</span>
                    <Badge variant="cyan">{s.proficiency}</Badge>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
