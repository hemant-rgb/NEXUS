import { describe, it, expect, vi } from "vitest";
import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import { OpportunityCard } from "../components/opportunities/opportunity-card";
import { Opportunity } from "../lib/types";

const mockOpportunity: Opportunity = {
  id: "test-opp-1",
  title: "Google Summer of Code 2027",
  organization: "Google Open Source",
  description: "Worldwide open source mentorship program.",
  category: "Google Summer of Code",
  opportunityType: "OPEN_SOURCE",
  location: "Global Remote",
  remoteAvailable: true,
  requiredSkills: ["Git & GitHub", "Python", "Java"],
  preferredSkills: ["Docker"],
  applicationUrl: "https://summerofcode.withgoogle.com",
  deadline: "2027-04-15",
  deadlineType: "STRICT",
  status: "OPEN",
  createdAt: new Date().toISOString(),
  lastVerifiedAt: new Date().toISOString(),
  isSaved: false,
};

describe("OpportunityCard Component", () => {
  it("renders opportunity card with title, organization and category", () => {
    render(<OpportunityCard opportunity={mockOpportunity} />);
    expect(screen.getByText("Google Summer of Code 2027")).toBeInTheDocument();
    expect(screen.getByText("Google Open Source")).toBeInTheDocument();
    expect(screen.getByText("Google Summer of Code")).toBeInTheDocument();
    expect(screen.getByText("Remote")).toBeInTheDocument();
  });

  it("triggers onToggleSave when bookmark button is clicked", async () => {
    const handleToggle = vi.fn().mockResolvedValue(undefined);
    render(<OpportunityCard opportunity={mockOpportunity} onToggleSave={handleToggle} />);

    const bookmarkBtn = screen.getByTitle(/Save opportunity/i);
    expect(bookmarkBtn).toBeInTheDocument();

    fireEvent.click(bookmarkBtn);

    expect(handleToggle).toHaveBeenCalledWith("test-opp-1", false);
  });
});
