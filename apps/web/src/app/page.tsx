"use client";

import React from "react";
import Link from "next/link";
import {
  Terminal,
  ArrowRight,
  ShieldCheck,
  Cpu,
  Layers,
  Compass,
  Sparkles,
  Map,
  GitBranch,
  Activity,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";

const WORKFLOW_STEPS = [
  { index: "01", name: "DISCOVER", desc: "Scan global developer opportunities, internships, and open-source programs." },
  { index: "02", name: "UNDERSTAND", desc: "Parse technical requirements, project scope, and contributor expectations." },
  { index: "03", name: "MATCH", desc: "Analyze profile compatibility using technical skills and experience levels." },
  { index: "04", name: "PREPARE", desc: "Generate targeted study tracks, repository issue prep, and roadmaps." },
  { index: "05", name: "EXECUTE", desc: "Build tailored applications, track proposals, and review contributions." },
  { index: "06", name: "TRACK", desc: "Monitor deadlines, milestone statuses, and program notifications." },
];

const MODULES = [
  { name: "NEXUS Scout", status: "Phase 2", desc: "Real-time index of open tech programs and internships." },
  { name: "NEXUS Intelligence", status: "Phase 2", desc: "Requirement decomposition and compatibility scoring." },
  { name: "NEXUS Strategy", status: "Phase 3", desc: "Personalized preparation milestones and skill paths." },
  { name: "NEXUS Execution", status: "Phase 3", desc: "Git workflow, proposal review, and submission assistant." },
  { name: "NEXUS Pulse", status: "Phase 4", desc: "Unified deadline monitor and telemetry tracking." },
];

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-bg-main text-text-primary flex flex-col font-sans">
      {/* Header */}
      <header className="border-b border-surface-border bg-bg-secondary/80 backdrop-blur-sm sticky top-0 z-50">
        <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="h-8 w-8 rounded-sm bg-brand flex items-center justify-center text-black font-mono font-bold text-base">
              N
            </div>
            <div className="flex flex-col">
              <span className="font-mono font-bold text-sm tracking-wider text-text-primary">
                NEXUS
              </span>
              <span className="font-mono text-[9px] uppercase tracking-widest text-text-muted">
                Opportunity Intelligence
              </span>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <Link href="/login">
              <Button variant="ghost" size="sm">
                Sign In
              </Button>
            </Link>
            <Link href="/register">
              <Button variant="primary" size="sm">
                Get Started
                <ArrowRight className="ml-1.5 h-3.5 w-3.5" />
              </Button>
            </Link>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="py-20 px-6 border-b border-surface-border bg-radial">
        <div className="max-w-4xl mx-auto text-center space-y-6">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-sm bg-surface-card border border-surface-border text-xs font-mono text-text-secondary">
            <span className="h-2 w-2 rounded-full bg-brand" />
            <span>Phase 1 Initialized: Identity &amp; Developer Foundation</span>
          </div>

          <h1 className="text-4xl sm:text-5xl lg:text-6xl font-mono font-bold tracking-tight text-text-primary leading-tight">
            Developer Opportunity Intelligence.
          </h1>

          <p className="text-base sm:text-lg text-text-secondary max-w-2xl mx-auto font-sans leading-relaxed">
            NEXUS is an AI-powered intelligence platform built for university students, open-source contributors, and software engineers to discover, evaluate, and execute opportunities strategically.
          </p>

          <div className="flex flex-wrap items-center justify-center gap-3 pt-4">
            <Link href="/register">
              <Button size="lg" className="px-8">
                Create Engineer Profile
                <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            </Link>
            <Link href="/login">
              <Button variant="outline" size="lg" className="px-8">
                Access Command Center
              </Button>
            </Link>
          </div>

          {/* Quick Technical Specs */}
          <div className="pt-10 flex flex-wrap justify-center gap-6 text-xs font-mono text-text-muted">
            <span className="flex items-center gap-1.5">
              <ShieldCheck className="h-3.5 w-3.5 text-brand" /> HttpOnly Secure Sessions
            </span>
            <span className="flex items-center gap-1.5">
              <Layers className="h-3.5 w-3.5 text-cyan" /> PostgreSQL &amp; Flyway
            </span>
            <span className="flex items-center gap-1.5">
              <Cpu className="h-3.5 w-3.5 text-warning" /> Java 21 &amp; Spring Boot
            </span>
          </div>
        </div>
      </section>

      {/* Workflow Section */}
      <section className="py-16 px-6 border-b border-surface-border bg-bg-secondary">
        <div className="max-w-6xl mx-auto space-y-8">
          <div>
            <span className="text-[11px] font-mono uppercase tracking-widest text-brand font-semibold">
              Execution Architecture
            </span>
            <h2 className="text-2xl font-mono font-bold text-text-primary mt-1">
              Core Platform Workflow
            </h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {WORKFLOW_STEPS.map((step) => (
              <div
                key={step.name}
                className="p-5 rounded-md bg-surface-card border border-surface-border flex flex-col justify-between space-y-3"
              >
                <div className="flex items-center justify-between">
                  <span className="font-mono text-xs font-bold text-brand">{step.index}</span>
                  <Badge variant="outline">{step.name}</Badge>
                </div>
                <p className="text-xs text-text-secondary leading-relaxed font-sans">{step.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Modules Roadmap Overview */}
      <section className="py-16 px-6 border-b border-surface-border">
        <div className="max-w-6xl mx-auto space-y-8">
          <div>
            <span className="text-[11px] font-mono uppercase tracking-widest text-cyan font-semibold">
              Platform Modules
            </span>
            <h2 className="text-2xl font-mono font-bold text-text-primary mt-1">
              System Capabilities &amp; Roadmap
            </h2>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            <div className="p-5 rounded-md bg-surface-card border border-brand/40 flex flex-col justify-between space-y-3">
              <div className="flex items-center justify-between">
                <span className="font-mono text-sm font-semibold text-text-primary">
                  Foundation &amp; Identity
                </span>
                <Badge variant="brand">Ready (Phase 1)</Badge>
              </div>
              <p className="text-xs text-text-secondary leading-relaxed">
                Authentication, session security, developer profile onboarding, and technical skills management.
              </p>
            </div>

            {MODULES.map((m) => (
              <div
                key={m.name}
                className="p-5 rounded-md bg-surface-card border border-surface-border flex flex-col justify-between space-y-3 opacity-80"
              >
                <div className="flex items-center justify-between">
                  <span className="font-mono text-sm font-semibold text-text-primary">{m.name}</span>
                  <Badge variant="secondary">{m.status}</Badge>
                </div>
                <p className="text-xs text-text-secondary leading-relaxed">{m.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="mt-auto py-8 px-6 bg-bg-secondary border-t border-surface-border">
        <div className="max-w-6xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-4 text-xs font-mono text-text-muted">
          <div className="flex items-center gap-2">
            <div className="h-5 w-5 rounded-sm bg-brand flex items-center justify-center text-black font-bold text-xs">
              N
            </div>
            <span>NEXUS — AI Opportunity Intelligence Platform</span>
          </div>
          <div>Built with Next.js, Spring Boot, and PostgreSQL.</div>
        </div>
      </footer>
    </div>
  );
}
