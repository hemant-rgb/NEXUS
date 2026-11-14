"use client";

import React, { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, Briefcase, Plus, Sparkles, Calendar, Link as LinkIcon, Building } from "lucide-react";
import { api } from "@/lib/api";
import { Opportunity, ApplicationStatus, ApplicationPriority } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

export default function NewApplicationPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const opportunityId = searchParams.get("opportunityId");

  const [loadingOpp, setLoadingOpp] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  // Form fields
  const [title, setTitle] = useState("");
  const [organization, setOrganization] = useState("");
  const [applicationType, setApplicationType] = useState("INTERNSHIP");
  const [applicationUrl, setApplicationUrl] = useState("");
  const [status, setStatus] = useState<ApplicationStatus>("PREPARING");
  const [priority, setPriority] = useState<ApplicationPriority>("MEDIUM");
  const [deadline, setDeadline] = useState("");
  const [expectedResponseDate, setExpectedResponseDate] = useState("");
  const [userNotes, setUserNotes] = useState("");

  useEffect(() => {
    if (opportunityId) {
      setLoadingOpp(true);
      api.opportunities
        .get(opportunityId)
        .then((opp) => {
          setTitle(opp.title);
          setOrganization(opp.organization);
          setApplicationType(opp.category || "INTERNSHIP");
          if (opp.applicationUrl) setApplicationUrl(opp.applicationUrl);
          if (opp.deadline) setDeadline(opp.deadline);
        })
        .catch((err) => console.error("Could not load linked opportunity", err))
        .finally(() => setLoadingOpp(false));
    }
  }, [opportunityId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !organization.trim()) return;

    setSubmitting(true);
    try {
      const created = await api.applications.create({
        opportunityId: opportunityId || undefined,
        title: title.trim(),
        organization: organization.trim(),
        applicationType,
        applicationUrl: applicationUrl.trim() || undefined,
        status,
        priority,
        deadline: deadline || undefined,
        expectedResponseDate: expectedResponseDate || undefined,
        userNotes: userNotes.trim() || undefined,
      });

      router.push(`/applications/${created.id}`);
    } catch (err) {
      console.error("Failed to create application", err);
      alert("Failed to create application. Check logs.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      {/* Back button */}
      <div>
        <Link
          href="/applications"
          className="inline-flex items-center gap-1.5 text-xs font-mono text-text-muted hover:text-text-primary transition-colors"
        >
          <ArrowLeft className="h-3.5 w-3.5" /> Back to Pipeline
        </Link>
      </div>

      <div className="p-6 rounded-lg border border-surface-border bg-surface-card space-y-6">
        <div className="border-b border-surface-border pb-4">
          <span className="font-mono text-xs uppercase tracking-widest text-brand font-semibold">
            Track Opportunity Application
          </span>
          <h1 className="text-xl font-mono font-bold text-text-primary mt-1">
            New Application Tracker
          </h1>
          <p className="text-xs font-mono text-text-secondary mt-1">
            Initialize an application workspace for automated document drafting, timeline audit logging, and interview tracking.
          </p>
        </div>

        {opportunityId && loadingOpp && (
          <div className="p-3 rounded bg-bg-main border border-surface-border text-xs font-mono text-text-muted">
            Prefilling from verified opportunity...
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4 font-mono text-xs">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-text-muted mb-1 font-semibold">Organization / Company *</label>
              <Input
                value={organization}
                onChange={(e) => setOrganization(e.target.value)}
                placeholder="e.g. Google, Stripe, Linux Foundation"
                required
                className="font-mono text-xs bg-bg-main"
              />
            </div>

            <div>
              <label className="block text-text-muted mb-1 font-semibold">Role / Position Title *</label>
              <Input
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="e.g. Software Engineering Intern"
                required
                className="font-mono text-xs bg-bg-main"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label className="block text-text-muted mb-1">Opportunity Type</label>
              <select
                value={applicationType}
                onChange={(e) => setApplicationType(e.target.value)}
                className="w-full p-2 bg-bg-main border border-surface-border rounded text-xs font-mono text-text-primary focus:outline-none focus:border-brand"
              >
                <option value="INTERNSHIP">Internship</option>
                <option value="FULL_TIME">Full-Time Job</option>
                <option value="GSOC">Google Summer of Code</option>
                <option value="OPEN_SOURCE">Open Source Program</option>
                <option value="HACKATHON">Hackathon</option>
                <option value="FELLOWSHIP">Fellowship</option>
                <option value="SCHOLARSHIP">Scholarship</option>
              </select>
            </div>

            <div>
              <label className="block text-text-muted mb-1">Initial Status</label>
              <select
                value={status}
                onChange={(e) => setStatus(e.target.value as ApplicationStatus)}
                className="w-full p-2 bg-bg-main border border-surface-border rounded text-xs font-mono text-text-primary focus:outline-none focus:border-brand"
              >
                <option value="INTERESTED">Interested</option>
                <option value="PREPARING">Preparing</option>
                <option value="READY_TO_APPLY">Ready to Apply</option>
                <option value="APPLIED">Applied</option>
              </select>
            </div>

            <div>
              <label className="block text-text-muted mb-1">Priority</label>
              <select
                value={priority}
                onChange={(e) => setPriority(e.target.value as ApplicationPriority)}
                className="w-full p-2 bg-bg-main border border-surface-border rounded text-xs font-mono text-text-primary focus:outline-none focus:border-brand"
              >
                <option value="HIGH">High Priority</option>
                <option value="MEDIUM">Medium Priority</option>
                <option value="LOW">Low Priority</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-text-muted mb-1">Application URL (Portal / Job Post)</label>
              <Input
                type="url"
                value={applicationUrl}
                onChange={(e) => setApplicationUrl(e.target.value)}
                placeholder="https://..."
                className="font-mono text-xs bg-bg-main"
              />
            </div>

            <div>
              <label className="block text-text-muted mb-1">Application Deadline</label>
              <Input
                type="date"
                value={deadline}
                onChange={(e) => setDeadline(e.target.value)}
                className="font-mono text-xs bg-bg-main"
              />
            </div>
          </div>

          <div>
            <label className="block text-text-muted mb-1">Developer Notes & Strategy</label>
            <textarea
              rows={3}
              value={userNotes}
              onChange={(e) => setUserNotes(e.target.value)}
              placeholder="Record target teams, key contacts, or preparation angles..."
              className="w-full p-2.5 bg-bg-main border border-surface-border rounded text-xs font-mono text-text-primary focus:outline-none focus:border-brand"
            />
          </div>

          <div className="flex items-center justify-end gap-3 pt-3 border-t border-surface-border">
            <Link href="/applications">
              <Button type="button" variant="outline" className="font-mono text-xs">
                Cancel
              </Button>
            </Link>
            <Button type="submit" disabled={submitting} className="font-mono text-xs">
              {submitting ? "Initializing..." : "Create Application"}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
