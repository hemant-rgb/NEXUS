"use client";

import React, { useState, useEffect } from "react";
import { Cpu, Plus, Trash2, Search, Edit2, Check, X } from "lucide-react";
import { api } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Skill, UserSkill, SkillProficiency } from "@/lib/types";

export default function SkillsPage() {
  const [userSkills, setUserSkills] = useState<UserSkill[]>([]);
  const [catalog, setCatalog] = useState<Skill[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // New Skill form
  const [selectedProficiency, setSelectedProficiency] = useState<SkillProficiency>("Intermediate");
  const [skillExperience, setSkillExperience] = useState(1.0);

  // Inline editing
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editProficiency, setEditProficiency] = useState<SkillProficiency>("Intermediate");
  const [editExperience, setEditExperience] = useState<number>(1.0);

  useEffect(() => {
    async function loadSkills() {
      try {
        const [userSkillsData, catalogData] = await Promise.all([
          api.skills.getUserSkills().catch(() => []),
          api.skills.catalog().catch(() => []),
        ]);
        setUserSkills(userSkillsData);
        setCatalog(catalogData);
      } finally {
        setLoading(false);
      }
    }
    loadSkills();
  }, []);

  const handleAddCatalogSkill = async (skill: Skill) => {
    setError(null);
    try {
      const added = await api.skills.addUserSkill({
        skillId: skill.id,
        proficiency: selectedProficiency,
        yearsExperience: skillExperience,
      });
      setUserSkills((prev) => [...prev, added]);
    } catch (err: any) {
      setError(err?.message || "Could not add skill");
    }
  };

  const handleAddCustomSkill = async () => {
    if (!searchQuery.trim()) return;
    setError(null);
    try {
      const added = await api.skills.addUserSkill({
        skillName: searchQuery.trim(),
        proficiency: selectedProficiency,
        yearsExperience: skillExperience,
      });
      setUserSkills((prev) => [...prev, added]);
      setSearchQuery("");
    } catch (err: any) {
      setError(err?.message || "Could not add custom skill");
    }
  };

  const handleDeleteSkill = async (id: string) => {
    setError(null);
    try {
      await api.skills.deleteUserSkill(id);
      setUserSkills((prev) => prev.filter((s) => s.id !== id));
    } catch (err: any) {
      setError("Failed to delete skill");
    }
  };

  const startEdit = (s: UserSkill) => {
    setEditingId(s.id);
    setEditProficiency(s.proficiency);
    setEditExperience(s.yearsExperience);
  };

  const saveEdit = async (id: string) => {
    try {
      const updated = await api.skills.updateUserSkill(id, {
        proficiency: editProficiency,
        yearsExperience: editExperience,
      });
      setUserSkills((prev) => prev.map((s) => (s.id === id ? updated : s)));
      setEditingId(null);
    } catch (err: any) {
      setError("Failed to update skill");
    }
  };

  if (loading) {
    return (
      <div className="flex h-96 items-center justify-center font-mono text-xs text-text-muted">
        Loading skills matrix...
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      {/* Header */}
      <div className="p-6 rounded-md bg-surface-card border border-surface-border flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <span className="text-[10px] font-mono uppercase tracking-widest text-brand font-semibold">
            Capabilities Engine
          </span>
          <h1 className="text-xl font-mono font-bold text-text-primary mt-0.5">
            Technical Skills Matrix
          </h1>
          <p className="text-xs font-mono text-text-muted">
            Manage your languages, frameworks, developer tools, and experience calibrations
          </p>
        </div>
        <Badge variant="cyan">{userSkills.length} Verified Skills</Badge>
      </div>

      {error && (
        <div className="p-3 rounded-sm bg-danger/10 border border-danger/30 text-xs font-mono text-danger">
          {error}
        </div>
      )}

      {/* Add Skill Panel */}
      <div className="p-5 rounded-md bg-surface-card border border-surface-border space-y-4">
        <span className="text-[11px] font-mono uppercase tracking-wider text-text-secondary font-semibold">
          Add New Skill to Matrix
        </span>

        <div className="grid grid-cols-1 sm:grid-cols-4 gap-3">
          <div className="sm:col-span-2 relative">
            <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-text-muted" />
            <Input
              placeholder="Search catalog or type custom skill..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-8 text-xs h-9"
            />
          </div>

          <div>
            <select
              value={selectedProficiency}
              onChange={(e) => setSelectedProficiency(e.target.value as any)}
              className="w-full h-9 rounded-md border border-surface-border bg-bg-secondary px-3 text-xs font-mono text-text-primary focus:border-brand focus:outline-none"
            >
              <option value="Beginner">Beginner</option>
              <option value="Intermediate">Intermediate</option>
              <option value="Advanced">Advanced</option>
            </select>
          </div>

          <div className="flex gap-2">
            <input
              type="number"
              step="0.5"
              min="0"
              max="30"
              value={skillExperience}
              onChange={(e) => setSkillExperience(parseFloat(e.target.value) || 0)}
              className="w-20 h-9 rounded-md border border-surface-border bg-bg-secondary px-2 text-xs font-mono text-text-primary text-center"
              placeholder="Yrs"
            />
            <Button
              variant="primary"
              size="sm"
              onClick={handleAddCustomSkill}
              disabled={!searchQuery.trim()}
              className="flex-1 h-9"
            >
              <Plus className="mr-1 h-3.5 w-3.5" /> Add
            </Button>
          </div>
        </div>

        {/* Quick Catalog Chips */}
        <div className="pt-2 border-t border-surface-border space-y-1.5">
          <span className="text-[10px] font-mono uppercase tracking-wider text-text-muted">
            Quick Add from Catalog
          </span>
          <div className="flex flex-wrap gap-1.5 max-h-32 overflow-y-auto">
            {catalog
              .filter(
                (c) =>
                  !userSkills.some((us) => us.skillName.toLowerCase() === c.name.toLowerCase()) &&
                  (!searchQuery || c.name.toLowerCase().includes(searchQuery.toLowerCase()))
              )
              .slice(0, 24)
              .map((skill) => (
                <button
                  key={skill.id}
                  onClick={() => handleAddCatalogSkill(skill)}
                  className="px-2.5 py-1 rounded-sm bg-bg-secondary hover:bg-surface-hover border border-surface-border text-xs font-mono text-text-secondary hover:text-text-primary transition-colors flex items-center gap-1"
                >
                  <span>{skill.name}</span>
                  <Plus className="h-3 w-3 text-brand" />
                </button>
              ))}
          </div>
        </div>
      </div>

      {/* User Skills Table */}
      <div className="p-5 rounded-md bg-surface-card border border-surface-border space-y-4">
        <span className="text-[11px] font-mono uppercase tracking-wider text-text-secondary font-semibold">
          Active Technical Skills ({userSkills.length})
        </span>

        {userSkills.length === 0 ? (
          <div className="p-8 text-center border border-dashed border-surface-border rounded-md font-mono text-xs text-text-muted">
            No skills in your matrix. Add from catalog or custom input above.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left font-mono text-xs">
              <thead>
                <tr className="border-b border-surface-border text-text-muted uppercase text-[10px] tracking-wider">
                  <th className="pb-3 font-semibold">Skill Name</th>
                  <th className="pb-3 font-semibold">Category</th>
                  <th className="pb-3 font-semibold">Proficiency</th>
                  <th className="pb-3 font-semibold">Experience</th>
                  <th className="pb-3 font-semibold text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-border/50">
                {userSkills.map((s) => {
                  const isEditing = editingId === s.id;
                  return (
                    <tr key={s.id} className="hover:bg-surface-hover/30 transition-colors">
                      <td className="py-3 font-medium text-text-primary">{s.skillName}</td>
                      <td className="py-3 text-text-secondary">{s.category}</td>
                      <td className="py-3">
                        {isEditing ? (
                          <select
                            value={editProficiency}
                            onChange={(e) => setEditProficiency(e.target.value as any)}
                            className="h-7 rounded-sm border border-surface-border bg-bg-secondary px-2 text-xs font-mono"
                          >
                            <option value="Beginner">Beginner</option>
                            <option value="Intermediate">Intermediate</option>
                            <option value="Advanced">Advanced</option>
                          </select>
                        ) : (
                          <Badge
                            variant={
                              s.proficiency === "Advanced"
                                ? "brand"
                                : s.proficiency === "Intermediate"
                                ? "cyan"
                                : "secondary"
                            }
                          >
                            {s.proficiency}
                          </Badge>
                        )}
                      </td>
                      <td className="py-3 text-text-secondary">
                        {isEditing ? (
                          <input
                            type="number"
                            step="0.5"
                            min="0"
                            max="30"
                            value={editExperience}
                            onChange={(e) => setEditExperience(parseFloat(e.target.value) || 0)}
                            className="w-16 h-7 rounded-sm border border-surface-border bg-bg-secondary px-2 text-xs font-mono"
                          />
                        ) : (
                          `${s.yearsExperience} yrs`
                        )}
                      </td>
                      <td className="py-3 text-right">
                        {isEditing ? (
                          <div className="flex items-center justify-end gap-1.5">
                            <button
                              onClick={() => saveEdit(s.id)}
                              className="p-1 text-success hover:bg-surface-hover rounded-sm"
                              title="Save"
                            >
                              <Check className="h-3.5 w-3.5" />
                            </button>
                            <button
                              onClick={() => setEditingId(null)}
                              className="p-1 text-text-muted hover:bg-surface-hover rounded-sm"
                              title="Cancel"
                            >
                              <X className="h-3.5 w-3.5" />
                            </button>
                          </div>
                        ) : (
                          <div className="flex items-center justify-end gap-1.5">
                            <button
                              onClick={() => startEdit(s)}
                              className="p-1 text-text-muted hover:text-text-primary hover:bg-surface-hover rounded-sm"
                              title="Edit"
                            >
                              <Edit2 className="h-3.5 w-3.5" />
                            </button>
                            <button
                              onClick={() => handleDeleteSkill(s.id)}
                              className="p-1 text-text-muted hover:text-danger hover:bg-surface-hover rounded-sm"
                              title="Remove"
                            >
                              <Trash2 className="h-3.5 w-3.5" />
                            </button>
                          </div>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
