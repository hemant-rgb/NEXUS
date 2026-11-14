"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import {
  User,
  GraduationCap,
  Cpu,
  Compass,
  CheckCircle2,
  ArrowRight,
  ArrowLeft,
  Plus,
  Trash2,
  Search,
  Sliders,
  Sparkles,
} from "lucide-react";
import { api } from "@/lib/api";
import { useAuth } from "@/lib/auth-context";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import {
  OPPORTUNITY_CATEGORIES,
  DEFAULT_INTERESTS,
  Skill,
  UserSkill,
  UserProfile,
  UserPreferences,
} from "@/lib/types";

const STEPS = [
  { id: 1, name: "Basic Info", icon: User },
  { id: 2, name: "Education", icon: GraduationCap },
  { id: 3, name: "Skills", icon: Cpu },
  { id: 4, name: "Interests", icon: Compass },
  { id: 5, name: "Preferences", icon: Sliders },
  { id: 6, name: "Review", icon: CheckCircle2 },
];

export default function OnboardingPage() {
  const router = useRouter();
  const { user } = useAuth();

  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Profile Form State
  const [fullName, setFullName] = useState("");
  const [countryCode, setCountryCode] = useState("US");
  const [timezone, setTimezone] = useState("UTC");
  const [institution, setInstitution] = useState("");
  const [educationLevel, setEducationLevel] = useState("Bachelor's");
  const [degree, setDegree] = useState("B.S.");
  const [fieldOfStudy, setFieldOfStudy] = useState("Computer Science");
  const [graduationYear, setGraduationYear] = useState(2026);
  const [experienceLevel, setExperienceLevel] = useState("Intermediate");
  const [bio, setBio] = useState("");
  const [githubUsername, setGithubUsername] = useState("");

  // Skills State
  const [userSkills, setUserSkills] = useState<UserSkill[]>([]);
  const [catalog, setCatalog] = useState<Skill[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedProficiency, setSelectedProficiency] = useState<"Beginner" | "Intermediate" | "Advanced">("Intermediate");
  const [skillExperience, setSkillExperience] = useState(1.0);

  // Preferences & Interests State
  const [selectedInterests, setSelectedInterests] = useState<string[]>([
    "Software Engineering",
    "Open Source",
  ]);
  const [customInterest, setCustomInterest] = useState("");
  const [selectedCategories, setSelectedCategories] = useState<string[]>([
    "Internships",
    "Google Summer of Code",
  ]);
  const [remotePreference, setRemotePreference] = useState("REMOTE");
  const [preferredCountries, setPreferredCountries] = useState<string[]>(["Remote", "US"]);
  const [countryInput, setCountryInput] = useState("");
  const [weeklyHours, setWeeklyHours] = useState(15);

  useEffect(() => {
    async function loadData() {
      try {
        const [profileData, prefData, skillsData, catalogData] = await Promise.all([
          api.profile.get().catch(() => null),
          api.preferences.get().catch(() => null),
          api.skills.getUserSkills().catch(() => []),
          api.skills.catalog().catch(() => []),
        ]);

        if (profileData) {
          if (profileData.fullName) setFullName(profileData.fullName);
          if (profileData.countryCode) setCountryCode(profileData.countryCode);
          if (profileData.timezone) setTimezone(profileData.timezone);
          if (profileData.institution) setInstitution(profileData.institution);
          if (profileData.educationLevel) setEducationLevel(profileData.educationLevel);
          if (profileData.degree) setDegree(profileData.degree);
          if (profileData.fieldOfStudy) setFieldOfStudy(profileData.fieldOfStudy);
          if (profileData.graduationYear) setGraduationYear(profileData.graduationYear);
          if (profileData.experienceLevel) setExperienceLevel(profileData.experienceLevel);
          if (profileData.bio) setBio(profileData.bio);
          if (profileData.githubUsername) setGithubUsername(profileData.githubUsername);
          if (profileData.onboardingStep) setStep(profileData.onboardingStep);
        } else if (user?.fullName) {
          setFullName(user.fullName);
        }

        if (prefData) {
          if (prefData.interests?.length) setSelectedInterests(prefData.interests);
          if (prefData.preferredCategories?.length) setSelectedCategories(prefData.preferredCategories);
          if (prefData.remotePreference) setRemotePreference(prefData.remotePreference);
          if (prefData.preferredCountries?.length) setPreferredCountries(prefData.preferredCountries);
          if (prefData.weeklyHours) setWeeklyHours(prefData.weeklyHours);
        }

        setUserSkills(skillsData);
        setCatalog(catalogData);
      } catch (err: any) {
        setError("Error loading profile details.");
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, [user]);

  const saveCurrentProgress = async (nextStep: number, complete = false) => {
    setSaving(true);
    setError(null);
    try {
      await Promise.all([
        api.profile.update({
          fullName,
          countryCode,
          timezone,
          institution,
          educationLevel,
          degree,
          fieldOfStudy,
          graduationYear,
          experienceLevel,
          bio,
          githubUsername,
          onboardingStep: nextStep,
          onboardingCompleted: complete,
        }),
        api.preferences.update({
          interests: selectedInterests,
          preferredCategories: selectedCategories,
          remotePreference,
          preferredCountries,
          weeklyHours,
        }),
      ]);

      if (complete) {
        router.push("/dashboard");
      } else {
        setStep(nextStep);
      }
    } catch (err: any) {
      setError(err?.message || "Failed to save progress.");
    } finally {
      setSaving(false);
    }
  };

  const handleAddSkill = async (skill: Skill) => {
    try {
      const added = await api.skills.addUserSkill({
        skillId: skill.id,
        proficiency: selectedProficiency,
        yearsExperience: skillExperience,
      });
      setUserSkills((prev) => [...prev, added]);
    } catch (err: any) {
      setError(err?.message || "Skill could not be added.");
    }
  };

  const handleAddCustomSkill = async () => {
    if (!searchQuery.trim()) return;
    try {
      const added = await api.skills.addUserSkill({
        skillName: searchQuery.trim(),
        proficiency: selectedProficiency,
        yearsExperience: skillExperience,
      });
      setUserSkills((prev) => [...prev, added]);
      setSearchQuery("");
    } catch (err: any) {
      setError(err?.message || "Custom skill could not be added.");
    }
  };

  const handleRemoveSkill = async (id: string) => {
    try {
      await api.skills.deleteUserSkill(id);
      setUserSkills((prev) => prev.filter((s) => s.id !== id));
    } catch (err: any) {
      setError("Failed to remove skill.");
    }
  };

  const toggleInterest = (interest: string) => {
    setSelectedInterests((prev) =>
      prev.includes(interest) ? prev.filter((i) => i !== interest) : [...prev, interest]
    );
  };

  const addCustomInterest = () => {
    if (customInterest.trim() && !selectedInterests.includes(customInterest.trim())) {
      setSelectedInterests((prev) => [...prev, customInterest.trim()]);
      setCustomInterest("");
    }
  };

  const toggleCategory = (cat: string) => {
    setSelectedCategories((prev) =>
      prev.includes(cat) ? prev.filter((c) => c !== cat) : [...prev, cat]
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
        Loading onboarding configuration...
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header & Steps Tracker */}
      <div className="p-5 rounded-md bg-surface-card border border-surface-border space-y-5">
        <div className="flex items-center justify-between">
          <div>
            <span className="text-[10px] font-mono uppercase tracking-widest text-brand font-semibold">
              Profile Onboarding
            </span>
            <h1 className="text-xl font-mono font-bold text-text-primary mt-0.5">
              Step {step} of 6 — {STEPS[step - 1].name}
            </h1>
          </div>
          <Button
            variant="outline"
            size="sm"
            onClick={() => saveCurrentProgress(step, false)}
            loading={saving}
          >
            Save Progress
          </Button>
        </div>

        {/* Step Timeline */}
        <div className="grid grid-cols-6 gap-2">
          {STEPS.map((s) => {
            const isCompleted = s.id < step;
            const isCurrent = s.id === step;
            const Icon = s.icon;
            return (
              <button
                key={s.id}
                onClick={() => setStep(s.id)}
                className={`p-2.5 rounded-sm border flex flex-col items-center gap-1.5 transition-colors text-left ${
                  isCurrent
                    ? "bg-surface-hover border-brand text-brand"
                    : isCompleted
                    ? "bg-surface-card border-surface-border text-success"
                    : "bg-bg-secondary border-surface-border text-text-muted hover:border-text-secondary"
                }`}
              >
                <div className="flex items-center gap-1">
                  <Icon className="h-3.5 w-3.5" />
                  <span className="text-[10px] font-mono font-bold">0{s.id}</span>
                </div>
                <span className="text-[10px] font-mono truncate w-full text-center">
                  {s.name}
                </span>
              </button>
            );
          })}
        </div>
      </div>

      {error && (
        <div className="p-3 rounded-sm bg-danger/10 border border-danger/30 text-xs font-mono text-danger">
          {error}
        </div>
      )}

      {/* STEP 1: Basic Information */}
      {step === 1 && (
        <div className="p-6 rounded-md bg-surface-card border border-surface-border space-y-5">
          <h2 className="text-base font-mono font-semibold text-text-primary">
            Personal &amp; Geographic Information
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input
              label="Full Name"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="Your full name"
            />
            <Input
              label="Country Code"
              value={countryCode}
              onChange={(e) => setCountryCode(e.target.value)}
              placeholder="US, IN, DE, etc."
            />
            <Input
              label="Timezone"
              value={timezone}
              onChange={(e) => setTimezone(e.target.value)}
              placeholder="America/New_York, UTC, etc."
            />
            <Input
              label="Institution / University"
              value={institution}
              onChange={(e) => setInstitution(e.target.value)}
              placeholder="e.g. UC Berkeley, IIT Bombay"
            />
            <div className="md:col-span-2">
              <Input
                label="GitHub Username"
                value={githubUsername}
                onChange={(e) => setGithubUsername(e.target.value)}
                placeholder="octocat"
                helperText="Will be used by NEXUS Execution module for contribution analysis"
              />
            </div>
            <div className="md:col-span-2 space-y-1.5">
              <label className="block text-[11px] font-mono uppercase tracking-wider text-text-secondary">
                Developer Bio
              </label>
              <textarea
                value={bio}
                onChange={(e) => setBio(e.target.value)}
                rows={3}
                className="w-full rounded-md border border-surface-border bg-bg-secondary px-3 py-2 text-sm text-text-primary placeholder:text-text-muted focus:border-brand focus:outline-none focus:ring-1 focus:ring-brand font-sans"
                placeholder="Brief summary of your technical interests and background..."
              />
            </div>
          </div>
        </div>
      )}

      {/* STEP 2: Education */}
      {step === 2 && (
        <div className="p-6 rounded-md bg-surface-card border border-surface-border space-y-5">
          <h2 className="text-base font-mono font-semibold text-text-primary">
            Academic Background
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <label className="block text-[11px] font-mono uppercase tracking-wider text-text-secondary">
                Education Level
              </label>
              <select
                value={educationLevel}
                onChange={(e) => setEducationLevel(e.target.value)}
                className="w-full h-9 rounded-md border border-surface-border bg-bg-secondary px-3 text-sm text-text-primary focus:border-brand focus:outline-none focus:ring-1 focus:ring-brand font-mono"
              >
                <option value="Undergraduate">Undergraduate / College</option>
                <option value="Bachelor's">Bachelor&apos;s Degree</option>
                <option value="Master's">Master&apos;s Degree</option>
                <option value="PhD">Doctorate / PhD</option>
                <option value="Self-Taught">Self-Taught Developer</option>
                <option value="Bootcamp">Coding Bootcamp</option>
              </select>
            </div>

            <Input
              label="Degree / Credential"
              value={degree}
              onChange={(e) => setDegree(e.target.value)}
              placeholder="e.g. B.S., B.Tech, M.S."
            />

            <Input
              label="Field / Branch of Study"
              value={fieldOfStudy}
              onChange={(e) => setFieldOfStudy(e.target.value)}
              placeholder="e.g. Computer Science, Electrical Eng."
            />

            <Input
              label="Expected Graduation Year"
              type="number"
              value={graduationYear}
              onChange={(e) => setGraduationYear(parseInt(e.target.value) || 2026)}
              min={1950}
              max={2040}
            />

            <div className="space-y-1.5">
              <label className="block text-[11px] font-mono uppercase tracking-wider text-text-secondary">
                Self-Evaluated Experience Level
              </label>
              <select
                value={experienceLevel}
                onChange={(e) => setExperienceLevel(e.target.value)}
                className="w-full h-9 rounded-md border border-surface-border bg-bg-secondary px-3 text-sm text-text-primary focus:border-brand focus:outline-none focus:ring-1 focus:ring-brand font-mono"
              >
                <option value="Beginner">Beginner (&lt; 1 yr)</option>
                <option value="Intermediate">Intermediate (1-3 yrs)</option>
                <option value="Advanced">Advanced (3+ yrs)</option>
              </select>
            </div>
          </div>
        </div>
      )}

      {/* STEP 3: Skills */}
      {step === 3 && (
        <div className="p-6 rounded-md bg-surface-card border border-surface-border space-y-6">
          <div>
            <h2 className="text-base font-mono font-semibold text-text-primary">
              Technical Skills Matrix
            </h2>
            <p className="text-xs font-mono text-text-muted mt-0.5">
              Add technical skills, languages, and tools to calibrate opportunity matching
            </p>
          </div>

          {/* User Skills List */}
          <div className="space-y-2">
            <span className="text-[11px] font-mono uppercase tracking-wider text-text-secondary">
              Current Skills ({userSkills.length})
            </span>
            {userSkills.length === 0 ? (
              <div className="p-4 rounded-md border border-dashed border-surface-border text-center font-mono text-xs text-text-muted">
                No skills added yet. Select from the catalog below or add a custom skill.
              </div>
            ) : (
              <div className="flex flex-wrap gap-2">
                {userSkills.map((us) => (
                  <div
                    key={us.id}
                    className="inline-flex items-center gap-2 px-3 py-1.5 rounded-sm bg-bg-secondary border border-surface-border text-xs font-mono"
                  >
                    <span className="font-medium text-text-primary">{us.skillName}</span>
                    <Badge variant="cyan">{us.proficiency}</Badge>
                    <span className="text-text-muted">{us.yearsExperience}y</span>
                    <button
                      onClick={() => handleRemoveSkill(us.id)}
                      className="text-text-muted hover:text-danger ml-1"
                    >
                      <Trash2 className="h-3 w-3" />
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Skill Selector Controls */}
          <div className="p-4 rounded-md bg-bg-secondary border border-surface-border space-y-3">
            <span className="text-[11px] font-mono uppercase tracking-wider text-brand font-semibold">
              Add Skill Config
            </span>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div>
                <label className="block text-[10px] font-mono text-text-muted mb-1">
                  Proficiency
                </label>
                <select
                  value={selectedProficiency}
                  onChange={(e) => setSelectedProficiency(e.target.value as any)}
                  className="w-full h-8 rounded-sm border border-surface-border bg-surface-card px-2 text-xs font-mono"
                >
                  <option value="Beginner">Beginner</option>
                  <option value="Intermediate">Intermediate</option>
                  <option value="Advanced">Advanced</option>
                </select>
              </div>

              <div>
                <label className="block text-[10px] font-mono text-text-muted mb-1">
                  Years Exp
                </label>
                <input
                  type="number"
                  step="0.5"
                  min="0"
                  max="30"
                  value={skillExperience}
                  onChange={(e) => setSkillExperience(parseFloat(e.target.value) || 0)}
                  className="w-full h-8 rounded-sm border border-surface-border bg-surface-card px-2 text-xs font-mono"
                />
              </div>

              <div className="flex items-end">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={handleAddCustomSkill}
                  disabled={!searchQuery.trim()}
                  className="w-full h-8"
                >
                  <Plus className="mr-1 h-3 w-3" /> Add Custom
                </Button>
              </div>
            </div>

            {/* Catalog Search */}
            <div className="relative mt-2">
              <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-text-muted" />
              <Input
                placeholder="Search catalog or type custom skill..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-8 text-xs h-8"
              />
            </div>

            {/* Available Catalog Badges */}
            <div className="max-h-40 overflow-y-auto pt-2 flex flex-wrap gap-1.5">
              {catalog
                .filter(
                  (c) =>
                    !userSkills.some((us) => us.skillName.toLowerCase() === c.name.toLowerCase()) &&
                    (!searchQuery || c.name.toLowerCase().includes(searchQuery.toLowerCase()))
                )
                .slice(0, 20)
                .map((skill) => (
                  <button
                    key={skill.id}
                    onClick={() => handleAddSkill(skill)}
                    className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-sm bg-surface-card hover:bg-surface-hover border border-surface-border text-xs font-mono text-text-secondary hover:text-text-primary transition-colors"
                  >
                    <span>{skill.name}</span>
                    <Plus className="h-3 w-3 text-brand" />
                  </button>
                ))}
            </div>
          </div>
        </div>
      )}

      {/* STEP 4: Professional Interests */}
      {step === 4 && (
        <div className="p-6 rounded-md bg-surface-card border border-surface-border space-y-6">
          <div>
            <h2 className="text-base font-mono font-semibold text-text-primary">
              Professional &amp; Technical Interests
            </h2>
            <p className="text-xs font-mono text-text-muted mt-0.5">
              Select key domains you want NEXUS to prioritize in your opportunity feed
            </p>
          </div>

          <div className="flex flex-wrap gap-2">
            {DEFAULT_INTERESTS.map((interest) => {
              const selected = selectedInterests.includes(interest);
              return (
                <button
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

          {/* Custom Interests */}
          <div className="pt-4 border-t border-surface-border space-y-3">
            <span className="text-[11px] font-mono uppercase tracking-wider text-text-secondary">
              Add Custom Interest
            </span>
            <div className="flex gap-2">
              <Input
                placeholder="e.g. Distributed Systems, Rust WASM, LLM Fine-tuning"
                value={customInterest}
                onChange={(e) => setCustomInterest(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter") {
                    e.preventDefault();
                    addCustomInterest();
                  }
                }}
              />
              <Button variant="secondary" size="md" onClick={addCustomInterest}>
                Add
              </Button>
            </div>

            {selectedInterests.filter((i) => !(DEFAULT_INTERESTS as readonly string[]).includes(i)).length > 0 && (
              <div className="flex flex-wrap gap-1.5 pt-2">
                {selectedInterests
                  .filter((i) => !(DEFAULT_INTERESTS as readonly string[]).includes(i))
                  .map((custom) => (
                    <div
                      key={custom}
                      className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-sm bg-surface-hover border border-surface-border text-xs font-mono text-text-primary"
                    >
                      <span>{custom}</span>
                      <button
                        onClick={() => toggleInterest(custom)}
                        className="text-text-muted hover:text-danger"
                      >
                        ×
                      </button>
                    </div>
                  ))}
              </div>
            )}
          </div>
        </div>
      )}

      {/* STEP 5: Opportunity Preferences */}
      {step === 5 && (
        <div className="p-6 rounded-md bg-surface-card border border-surface-border space-y-6">
          <div>
            <h2 className="text-base font-mono font-semibold text-text-primary">
              Opportunity Preferences
            </h2>
            <p className="text-xs font-mono text-text-muted mt-0.5">
              Specify supported program categories, location preferences, and preparation bandwidth
            </p>
          </div>

          <div className="space-y-2">
            <span className="text-[11px] font-mono uppercase tracking-wider text-text-secondary">
              Opportunity Categories
            </span>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
              {OPPORTUNITY_CATEGORIES.map((cat) => {
                const selected = selectedCategories.includes(cat);
                return (
                  <button
                    key={cat}
                    onClick={() => toggleCategory(cat)}
                    className={`p-2.5 rounded-sm font-mono text-xs border text-left transition-colors flex items-center justify-between ${
                      selected
                        ? "bg-brand/15 border-brand text-brand font-medium"
                        : "bg-bg-secondary border-surface-border text-text-secondary hover:border-text-secondary"
                    }`}
                  >
                    <span className="truncate">{cat}</span>
                    {selected && <span className="text-brand text-xs font-bold">✓</span>}
                  </button>
                );
              })}
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 border-t border-surface-border">
            <div className="space-y-1.5">
              <label className="block text-[11px] font-mono uppercase tracking-wider text-text-secondary">
                Location Preference
              </label>
              <select
                value={remotePreference}
                onChange={(e) => setRemotePreference(e.target.value)}
                className="w-full h-9 rounded-md border border-surface-border bg-bg-secondary px-3 text-sm text-text-primary focus:border-brand focus:outline-none focus:ring-1 focus:ring-brand font-mono"
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
                Target Countries / Regions
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
                <Button variant="secondary" size="md" onClick={addCountry}>
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
                    <button onClick={() => removeCountry(c)} className="text-text-muted hover:text-danger">
                      ×
                    </button>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* STEP 6: Review & Finalize */}
      {step === 6 && (
        <div className="p-6 rounded-md bg-surface-card border border-surface-border space-y-6">
          <div>
            <h2 className="text-base font-mono font-semibold text-text-primary">
              Profile Summary &amp; Verification
            </h2>
            <p className="text-xs font-mono text-text-muted mt-0.5">
              Verify your developer profile settings. You can update these anytime in Settings.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="p-4 rounded-md bg-bg-secondary border border-surface-border space-y-2">
              <span className="text-[10px] font-mono uppercase tracking-wider text-brand font-semibold">
                Developer Identity
              </span>
              <p className="text-sm font-mono font-medium text-text-primary">{fullName || "Not provided"}</p>
              <p className="text-xs font-mono text-text-secondary">
                {institution ? `${institution} · ` : ""} {countryCode} · {timezone}
              </p>
              {githubUsername && (
                <p className="text-xs font-mono text-cyan">GitHub: @{githubUsername}</p>
              )}
            </div>

            <div className="p-4 rounded-md bg-bg-secondary border border-surface-border space-y-2">
              <span className="text-[10px] font-mono uppercase tracking-wider text-brand font-semibold">
                Education &amp; Experience
              </span>
              <p className="text-sm font-mono font-medium text-text-primary">
                {degree ? `${degree} in ${fieldOfStudy}` : educationLevel}
              </p>
              <p className="text-xs font-mono text-text-secondary">
                Graduation: {graduationYear} · Level: {experienceLevel}
              </p>
            </div>

            <div className="p-4 rounded-md bg-bg-secondary border border-surface-border space-y-2 md:col-span-2">
              <span className="text-[10px] font-mono uppercase tracking-wider text-brand font-semibold">
                Skills ({userSkills.length})
              </span>
              <div className="flex flex-wrap gap-1.5 pt-1">
                {userSkills.map((us) => (
                  <Badge key={us.id} variant="secondary">
                    {us.skillName} ({us.proficiency})
                  </Badge>
                ))}
              </div>
            </div>

            <div className="p-4 rounded-md bg-bg-secondary border border-surface-border space-y-2 md:col-span-2">
              <span className="text-[10px] font-mono uppercase tracking-wider text-brand font-semibold">
                Preferences &amp; Interests
              </span>
              <p className="text-xs font-mono text-text-secondary">
                Categories: {selectedCategories.join(", ") || "None"}
              </p>
              <p className="text-xs font-mono text-text-secondary">
                Location: {remotePreference} · Bandwidth: {weeklyHours} hrs/week
              </p>
              <p className="text-xs font-mono text-text-secondary">
                Interests: {selectedInterests.join(", ") || "None"}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Navigation Controls */}
      <div className="p-4 rounded-md bg-surface-card border border-surface-border flex items-center justify-between">
        <Button
          variant="outline"
          size="md"
          disabled={step === 1 || saving}
          onClick={() => setStep((s) => Math.max(1, s - 1))}
        >
          <ArrowLeft className="mr-1.5 h-3.5 w-3.5" /> Back
        </Button>

        {step < 6 ? (
          <Button
            variant="primary"
            size="md"
            loading={saving}
            onClick={() => saveCurrentProgress(step + 1, false)}
          >
            Continue
            <ArrowRight className="ml-1.5 h-3.5 w-3.5" />
          </Button>
        ) : (
          <Button
            variant="primary"
            size="md"
            loading={saving}
            onClick={() => saveCurrentProgress(6, true)}
          >
            Complete Onboarding &amp; Enter Console
            <CheckCircle2 className="ml-1.5 h-3.5 w-3.5" />
          </Button>
        )}
      </div>
    </div>
  );
}
