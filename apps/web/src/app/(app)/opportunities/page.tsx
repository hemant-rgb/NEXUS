"use client";

import React, { useState, useEffect, useCallback } from "react";
import { Compass, AlertCircle, ChevronLeft, ChevronRight, Layers } from "lucide-react";
import { api } from "@/lib/api";
import { Opportunity, PageResponse } from "@/lib/types";
import { OpportunityCard } from "@/components/opportunities/opportunity-card";
import { OpportunityFilterBar } from "@/components/opportunities/opportunity-filter-bar";
import { Button } from "@/components/ui/button";

export default function OpportunitiesPage() {
  const [opportunities, setOpportunities] = useState<Opportunity[]>([]);
  const [pageData, setPageData] = useState<PageResponse<Opportunity> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filters
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("");
  const [remoteOnly, setRemoteOnly] = useState(false);
  const [sortBy, setSortBy] = useState("newest");
  const [currentPage, setCurrentPage] = useState(0);

  const fetchOpportunities = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.opportunities.list({
        query: searchQuery.trim() || undefined,
        category: selectedCategory || undefined,
        remoteOnly: remoteOnly ? true : undefined,
        sortBy,
        page: currentPage,
        size: 12,
      });
      setPageData(res);
      setOpportunities(res.content);
    } catch (err: any) {
      setError(err?.message || "Failed to load opportunities from catalog.");
    } finally {
      setLoading(false);
    }
  }, [searchQuery, selectedCategory, remoteOnly, sortBy, currentPage]);

  useEffect(() => {
    fetchOpportunities();
  }, [fetchOpportunities]);

  const handleToggleSave = async (opportunityId: string, currentSaved: boolean) => {
    if (currentSaved) {
      await api.opportunities.removeSaved(opportunityId);
    } else {
      await api.opportunities.save(opportunityId);
    }
    // Update local state
    setOpportunities((prev) =>
      prev.map((o) => (o.id === opportunityId ? { ...o, isSaved: !currentSaved } : o))
    );
  };

  const handleResetFilters = () => {
    setSearchQuery("");
    setSelectedCategory("");
    setRemoteOnly(false);
    setSortBy("newest");
    setCurrentPage(0);
  };

  const hasActiveFilters = Boolean(searchQuery || selectedCategory || remoteOnly || sortBy !== "newest");

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div>
          <span className="text-[10px] font-mono uppercase tracking-widest text-brand font-semibold">
            NEXUS Scout Engine
          </span>
          <h1 className="text-xl font-mono font-bold text-text-primary mt-0.5">
            Opportunity Discovery
          </h1>
          <p className="text-xs font-mono text-text-muted">
            Explore verified open-source programs, internships, hackathons, and fellowships
          </p>
        </div>

        {pageData && (
          <div className="text-xs font-mono text-text-secondary bg-surface-card px-3 py-1.5 rounded-md border border-surface-border">
            Showing <span className="font-semibold text-text-primary">{pageData.totalElements}</span> opportunities
          </div>
        )}
      </div>

      {/* Filter Bar */}
      <OpportunityFilterBar
        searchQuery={searchQuery}
        onSearchChange={(q) => {
          setSearchQuery(q);
          setCurrentPage(0);
        }}
        selectedCategory={selectedCategory}
        onCategoryChange={(cat) => {
          setSelectedCategory(cat);
          setCurrentPage(0);
        }}
        remoteOnly={remoteOnly}
        onRemoteOnlyToggle={() => {
          setRemoteOnly(!remoteOnly);
          setCurrentPage(0);
        }}
        sortBy={sortBy}
        onSortByChange={(s) => {
          setSortBy(s);
          setCurrentPage(0);
        }}
        onReset={handleResetFilters}
        hasActiveFilters={hasActiveFilters}
      />

      {/* Error State */}
      {error && (
        <div className="p-4 rounded-md bg-danger/10 border border-danger/30 flex items-center gap-3 text-xs font-mono text-danger">
          <AlertCircle className="h-4 w-4 shrink-0" />
          <span>{error}</span>
          <Button variant="outline" size="sm" onClick={fetchOpportunities} className="ml-auto">
            Retry
          </Button>
        </div>
      )}

      {/* Loading Skeletons */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <div
              key={i}
              className="p-5 rounded-md bg-surface-card border border-surface-border space-y-4 animate-pulse h-48"
            >
              <div className="h-4 bg-surface-hover rounded w-1/3" />
              <div className="h-5 bg-surface-hover rounded w-3/4" />
              <div className="h-3 bg-surface-hover rounded w-full" />
              <div className="h-3 bg-surface-hover rounded w-2/3" />
            </div>
          ))}
        </div>
      ) : opportunities.length === 0 ? (
        /* Empty State */
        <div className="p-12 text-center rounded-md border border-dashed border-surface-border bg-surface-card/40 space-y-3">
          <Layers className="h-8 w-8 text-text-muted mx-auto" />
          <h3 className="font-mono text-sm font-semibold text-text-primary">
            No matching opportunities found
          </h3>
          <p className="text-xs font-sans text-text-muted max-w-sm mx-auto">
            Try adjusting your search query, clearing filters, or switching categories.
          </p>
          {hasActiveFilters && (
            <Button variant="outline" size="sm" onClick={handleResetFilters} className="mt-2">
              Clear All Filters
            </Button>
          )}
        </div>
      ) : (
        /* Opportunities Grid */
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {opportunities.map((opp) => (
            <OpportunityCard
              key={opp.id}
              opportunity={opp}
              onToggleSave={handleToggleSave}
            />
          ))}
        </div>
      )}

      {/* Pagination Controls */}
      {pageData && pageData.totalPages > 1 && (
        <div className="p-3 rounded-md bg-surface-card border border-surface-border flex items-center justify-between font-mono text-xs">
          <span className="text-text-muted">
            Page {pageData.number + 1} of {pageData.totalPages}
          </span>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              disabled={pageData.first || loading}
              onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
            >
              <ChevronLeft className="h-3.5 w-3.5 mr-1" /> Previous
            </Button>
            <Button
              variant="outline"
              size="sm"
              disabled={pageData.last || loading}
              onClick={() => setCurrentPage((p) => p + 1)}
            >
              Next <ChevronRight className="h-3.5 w-3.5 ml-1" />
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
