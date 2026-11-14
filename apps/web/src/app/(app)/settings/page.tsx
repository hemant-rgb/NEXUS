"use client";

import React, { useState, useEffect } from "react";
import { Sliders, Save, CheckCircle2, Shield, Lock } from "lucide-react";
import { api } from "@/lib/api";
import { useAuth } from "@/lib/auth-context";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { OPPORTUNITY_CATEGORIES, DEFAULT_INTERESTS, UserPreferences } from "@/lib/types";

export default function SettingsPage() {
  const { user } = useAuth();
  const [preferences, setPreferences] = useState<UserPreferences | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Editable Preferences State
  const [selectedCategories, setSelectedCategories] = useState<string[]>([]);
  const [remotePreference, setRemotePreference] = useState("REMOTE");
  const [preferredCountries, setPreferredCountries] = useState<string[]>([]);
  const [weeklyHours, setWeeklyHours] = useState(15);
  const [selectedInterests, setSelectedInterests] = useState<string[]>([]);
  const [countryInput, setCountryInput] = useState("");
  const [aiStatus, setAiStatus] = useState<Record<string, unknown> | null>(null);

  useEffect(() => {
    async function loadPreferences() {
      try {
        const [data, ai] = await Promise.all([
          api.preferences.get(),
          api.ai.getStatus().catch(() => null),
        ]);
        setPreferences(data);
        if (ai) setAiStatus(ai);
        if (data.preferredCategories) setSelectedCategories(data.preferredCategories);
        if (data.remotePreference) setRemotePreference(data.remotePreference);
        if (data.preferredCountries) setPreferredCountries(data.preferredCountries);
        if (data.weeklyHours) setWeeklyHours(data.weeklyHours);
        if (data.interests) setSelectedInterests(data.interests);
      } catch (err: any) {
        setError("Failed to load user preferences.");
      } finally {
        setLoading(false);
      }
    }
    loadPreferences();
  }, []);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setSuccess(false);

    try {
      const updated = await api.preferences.update({
        preferredCategories: selectedCategories,
        remotePreference,
        preferredCountries,
        weeklyHours,
        interests: selectedInterests,
      });
      setPreferences(updated);
      setSuccess(true);
      setTimeout(() => setSuccess(false), 3000);
    } catch (err: any) {
      setError(err?.message || "Failed to save preferences.");
    } finally {
      setSaving(false);
    }
  };

  const toggleCategory = (cat: string) => {
    setSelectedCategories((prev) =>
      prev.includes(cat) ? prev.filter((c) => c !== cat) : [...prev, cat]
    );
  };

  const toggleInterest = (interest: string) => {
    setSelectedInterests((prev) =>
      prev.includes(interest) ? prev.filter((i) => i !== interest) : [...prev, interest]
    );
  };

  const addCountry = () => {
    if (countryInput.trim() && !preferredCountries.includes(countryInput.trim())) {
      setPreferredCountries((prev) => [...prev, countryInput.trim()]);
      setCountryInput("");
    }
  };

  const removeCountry = (c: string) => {
    setPreferredCountries((prev) => prev.filter((item) => item !== c));
  };

  if (loading) {
    return (
      <div className="flex h-96 items-center justify-center font-mono text-xs text-text-muted">
        Loading preferences...
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="p-6 rounded-md bg-surface-card border border-surface-border flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <span className="text-[10px] font-mono uppercase tracking-widest text-brand font-semibold">
            Preferences &amp; Configuration
          </span>
          <h1 className="text-xl font-mono font-bold text-text-primary mt-0.5">
            Account &amp; Engine Settings
          </h1>
          <p className="text-xs font-mono text-text-muted">
            Configure opportunity filters, telemetry options, and session credentials
          </p>
        </div>
      </div>

      {success && (
        <div className="p-3 rounded-sm bg-success/15 border border-success/30 flex items-center gap-2 text-xs font-mono text-success">
          <CheckCircle2 className="h-4 w-4" />
          <span>Preferences updated and persisted successfully.</span>
        </div>
      )}

      {error && (
        <div className="p-3 rounded-sm bg-danger/10 border border-danger/30 text-xs font-mono text-danger">
          {error}
        </div>
      )}

      <form onSubmit={handleSave} className="space-y-6">
        {/* Opportunity Filter Preferences */}
        <div className="p-5 rounded-md bg-surface-card border border-surface-border space-y-5">
          <span className="text-[11px] font-mono uppercase tracking-wider text-text-secondary font-semibold">
            Opportunity Category Subscriptions
          </span>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
            {OPPORTUNITY_CATEGORIES.map((cat) => {
              const selected = selectedCategories.includes(cat);
              return (
                <button
                  type="button"
                  key={cat}
                  onClick={() => toggleCategory(cat)}
                  className={`p-2.5 rounded-sm font-mono text-xs border text-left transition-colors flex items-center justify-between ${
                    selected
                      ? "bg-brand/15 border-brand text-brand font-medium"
                      : "bg-bg-secondary border-surface-border text-text-secondary hover:border-text-secondary"
                  }`}
                >
                  <span className="truncate">{cat}</span>
                  {selected && <span className="text-brand font-bold">✓</span>}
                </button>
              );
            })}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 border-t border-surface-border">
            <div className="space-y-1.5">
              <label className="block text-[11px] font-mono uppercase tracking-wider text-text-secondary">
                Location Delivery Model
              </label>
              <select
                value={remotePreference}
                onChange={(e) => setRemotePreference(e.target.value)}
                className="w-full h-9 rounded-md border border-surface-border bg-bg-secondary px-3 text-xs font-mono text-text-primary focus:border-brand focus:outline-none"
              >
                <option value="REMOTE">Fully Remote</option>
                <option value="ONSITE">On-Site Only</option>
                <option value="HYBRID">Hybrid</option>
                <option value="ANY">No Preference / Any</option>
              </select>
            </div>

            <div className="space-y-1.5">
              <label className="block text-[11px] font-mono uppercase tracking-wider text-text-secondary">
                Weekly Preparation Bandwidth (Hours)
              </label>
              <Input
                type="number"
                min={1}
                max={60}
                value={weeklyHours}
                onChange={(e) => setWeeklyHours(parseInt(e.target.value) || 10)}
              />
            </div>

            <div className="md:col-span-2 space-y-2">
              <span className="text-[11px] font-mono uppercase tracking-wider text-text-secondary">
                Target Countries &amp; Jurisdictions
              </span>
              <div className="flex gap-2">
                <Input
                  placeholder="e.g. Remote, US, Germany, India, Singapore"
                  value={countryInput}
                  onChange={(e) => setCountryInput(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === "Enter") {
                      e.preventDefault();
                      addCountry();
                    }
                  }}
                />
                <Button type="button" variant="secondary" size="md" onClick={addCountry}>
                  Add Region
                </Button>
              </div>
              <div className="flex flex-wrap gap-1.5 pt-1">
                {preferredCountries.map((c) => (
                  <div
                    key={c}
                    className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-sm bg-surface-hover border border-surface-border text-xs font-mono text-text-primary"
                  >
                    <span>{c}</span>
                    <button
                      type="button"
                      onClick={() => removeCountry(c)}
                      className="text-text-muted hover:text-danger"
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Technical Interests */}
        <div className="p-5 rounded-md bg-surface-card border border-surface-border space-y-4">
          <span className="text-[11px] font-mono uppercase tracking-wider text-text-secondary font-semibold">
            Domain Focus &amp; Interests
          </span>
          <div className="flex flex-wrap gap-2">
            {DEFAULT_INTERESTS.map((interest) => {
              const selected = selectedInterests.includes(interest);
              return (
                <button
                  type="button"
                  key={interest}
                  onClick={() => toggleInterest(interest)}
                  className={`px-3 py-1.5 rounded-sm font-mono text-xs border transition-colors ${
                    selected
                      ? "bg-brand/15 border-brand text-brand font-medium"
                      : "bg-bg-secondary border-surface-border text-text-secondary hover:border-text-secondary"
                  }`}
                >
                  {selected ? "✓ " : "+ "}
                  {interest}
                </button>
              );
            })}
          </div>
        </div>

        {/* AI & Platform Security Architecture */}
        <div className="p-5 rounded-md bg-surface-card border border-surface-border space-y-4 font-mono text-xs">
          <div className="flex items-center justify-between border-b border-surface-border pb-3">
            <div className="flex items-center gap-2">
              <Shield className="h-4 w-4 text-brand" />
              <span className="text-xs font-bold uppercase tracking-wider text-text-primary">
                AI &amp; Platform Security Telemetry
              </span>
            </div>
            <span className="px-2 py-0.5 rounded text-[10px] bg-brand/10 border border-brand/30 text-brand">
              Production Hardened
            </span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
            <div className="p-3 bg-bg-secondary border border-surface-border rounded space-y-1">
              <div className="text-text-muted text-[10px] uppercase">Active AI Provider</div>
              <div className="text-text-primary font-bold">
                {String(aiStatus?.activeProviderName || "nexus-local-intelligence")}
              </div>
              <p className="text-[11px] text-text-secondary">
                {aiStatus?.isGroundedFallbackActive
                  ? "Operating in zero-latency deterministic offline mode."
                  : "External LLM reasoning with grounded policy guardrails."}
              </p>
            </div>

            <div className="p-3 bg-bg-secondary border border-surface-border rounded space-y-1">
              <div className="text-text-muted text-[10px] uppercase">Safety Governance</div>
              <div className="text-success font-bold flex items-center gap-1.5">
                <Lock className="h-3 w-3" /> Level 3 Human Approval Gated
              </div>
              <p className="text-[11px] text-text-secondary">
                Consequential external actions require explicit one-time authorization before execution.
              </p>
            </div>
          </div>
        </div>

        {/* Submit */}
        <div className="flex justify-end">
          <Button type="submit" loading={saving} size="md">
            <Save className="mr-1.5 h-3.5 w-3.5" /> Save Preferences
          </Button>
        </div>
      </form>
    </div>
  );
}
