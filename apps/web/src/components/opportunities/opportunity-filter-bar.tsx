"use client";

import React from "react";
import { Search, SlidersHorizontal, X } from "lucide-react";
import { Input } from "@/components/ui/input";
import { OPPORTUNITY_CATEGORIES } from "@/lib/types";

interface OpportunityFilterBarProps {
  searchQuery: string;
  onSearchChange: (val: string) => void;
  selectedCategory: string;
  onCategoryChange: (cat: string) => void;
  remoteOnly: boolean;
  onRemoteOnlyToggle: () => void;
  sortBy: string;
  onSortByChange: (sort: string) => void;
  onReset: () => void;
  hasActiveFilters: boolean;
}

export function OpportunityFilterBar({
  searchQuery,
  onSearchChange,
  selectedCategory,
  onCategoryChange,
  remoteOnly,
  onRemoteOnlyToggle,
  sortBy,
  onSortByChange,
  onReset,
  hasActiveFilters,
}: OpportunityFilterBarProps) {
  return (
    <div className="p-4 rounded-md bg-surface-card border border-surface-border space-y-3">
      {/* Top Search & Controls */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-text-muted" />
          <Input
            placeholder="Search opportunities, roles, organizations, or skills..."
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            className="pl-9 h-9 text-xs"
          />
        </div>

        <div className="flex items-center gap-2">
          {/* Remote Toggle */}
          <button
            onClick={onRemoteOnlyToggle}
            className={`px-3 h-9 rounded-md border text-xs font-mono transition-colors flex items-center gap-1.5 shrink-0 ${
              remoteOnly
                ? "bg-brand/15 border-brand text-brand font-medium"
                : "bg-bg-secondary border-surface-border text-text-secondary hover:border-text-secondary"
            }`}
          >
            <span>Remote Only</span>
            {remoteOnly && <span>✓</span>}
          </button>

          {/* Sort Select */}
          <select
            value={sortBy}
            onChange={(e) => onSortByChange(e.target.value)}
            className="h-9 rounded-md border border-surface-border bg-bg-secondary px-3 text-xs font-mono text-text-primary focus:border-brand focus:outline-none shrink-0"
          >
            <option value="newest">Newest Added</option>
            <option value="deadlineAsc">Deadline: Soonest</option>
            <option value="deadlineDesc">Deadline: Latest</option>
          </select>

          {hasActiveFilters && (
            <button
              onClick={onReset}
              className="px-2.5 h-9 rounded-md border border-surface-border bg-surface-hover text-text-secondary hover:text-text-primary text-xs font-mono flex items-center gap-1 shrink-0"
              title="Reset Filters"
            >
              <X className="h-3.5 w-3.5" />
              <span className="hidden sm:inline">Reset</span>
            </button>
          )}
        </div>
      </div>

      {/* Category Horizontal Pills */}
      <div className="flex items-center gap-1.5 overflow-x-auto pb-1 pt-1 scrollbar-none">
        <button
          onClick={() => onCategoryChange("")}
          className={`px-3 py-1 rounded-sm text-xs font-mono shrink-0 transition-colors border ${
            selectedCategory === ""
              ? "bg-surface-hover border-brand text-brand font-medium"
              : "bg-bg-secondary border-surface-border text-text-secondary hover:text-text-primary hover:border-text-secondary"
          }`}
        >
          All Categories
        </button>

        {OPPORTUNITY_CATEGORIES.map((cat) => {
          const isSelected = selectedCategory.toLowerCase() === cat.toLowerCase();
          return (
            <button
              key={cat}
              onClick={() => onCategoryChange(isSelected ? "" : cat)}
              className={`px-3 py-1 rounded-sm text-xs font-mono shrink-0 transition-colors border ${
                isSelected
                  ? "bg-surface-hover border-brand text-brand font-medium"
                  : "bg-bg-secondary border-surface-border text-text-secondary hover:text-text-primary hover:border-text-secondary"
              }`}
            >
              {cat}
            </button>
          );
        })}
      </div>
    </div>
  );
}
