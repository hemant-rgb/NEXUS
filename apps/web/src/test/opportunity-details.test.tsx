import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import OpportunityDetailsPage from "@/app/(app)/opportunities/[id]/page";
import { api } from "@/lib/api";
import { Opportunity } from "@/lib/types";

vi.mock("next/navigation", () => ({
  useParams: () => ({ id: "opp-123" }),
  useRouter: () => ({ push: vi.fn() }),
}));

vi.mock("@/lib/api", () => ({
  api: {
    opportunities: {
      get: vi.fn(),
      saved: vi.fn(),
      save: vi.fn(),
      removeSaved: vi.fn(),
      updateNotes: vi.fn(),
    },
  },
}));

describe("Opportunity Details Page", () => {
  const mockOpportunity: Opportunity = {
    id: "opp-123",
    title: "Google Summer of Code 2027 Contributor",
    organization: "Google Open Source",
    description: "Write code with mentor organizations.",
    category: "Google Summer of Code",
    opportunityType: "OPEN_SOURCE",
    location: "Global",
    countryCode: "GLOBAL",
    remoteAvailable: true,
    eligibilityCriteria: "Open to students and open source newcomers.",
    requiredSkills: ["Git", "Python"],
    preferredSkills: ["Docker", "Go"],
    applicationUrl: "https://summerofcode.withgoogle.com",
    officialWebsiteUrl: "https://summerofcode.withgoogle.com",
    deadline: "2027-04-15",
    deadlineType: "STRICT",
    status: "OPEN",
    isSaved: false,
    createdAt: "2026-11-14T09:00:00Z",
    lastVerifiedAt: "2026-11-14T09:00:00Z",
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders opportunity details with skills and criteria", async () => {
    vi.mocked(api.opportunities.get).mockResolvedValueOnce(mockOpportunity);

    render(<OpportunityDetailsPage />);

    expect(screen.getByText(/Loading opportunity specification/i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText("Google Summer of Code 2027 Contributor")).toBeInTheDocument();
      expect(screen.getByText("Google Open Source")).toBeInTheDocument();
      expect(screen.getByText(/Write code with mentor organizations/i)).toBeInTheDocument();
      expect(screen.getByText("Git")).toBeInTheDocument();
      expect(screen.getByText("Python")).toBeInTheDocument();
      expect(screen.getByText("Docker")).toBeInTheDocument();
      expect(screen.getByText("Go")).toBeInTheDocument();
      expect(screen.getByText(/2027-04-15/)).toBeInTheDocument();
    });
  });

  it("handles error state when opportunity fails to load", async () => {
    vi.mocked(api.opportunities.get).mockRejectedValueOnce(new Error("Opportunity not found"));

    render(<OpportunityDetailsPage />);

    await waitFor(() => {
      expect(screen.getByText(/Opportunity not found/i)).toBeInTheDocument();
      expect(screen.getByText(/Back to Discovery/i)).toBeInTheDocument();
    });
  });

  it("allows user to save opportunity and enter developer notes", async () => {
    vi.mocked(api.opportunities.get).mockResolvedValueOnce(mockOpportunity);
    vi.mocked(api.opportunities.save).mockResolvedValueOnce({
      id: "saved-1",
      opportunityId: "opp-123",
      opportunity: mockOpportunity,
      savedAt: "2026-11-14T10:00:00Z",
      notes: "Proposal draft ready",
    });

    render(<OpportunityDetailsPage />);

    await waitFor(() => {
      expect(screen.getByText("Save Opportunity")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText("Save Opportunity"));

    await waitFor(() => {
      expect(api.opportunities.save).toHaveBeenCalledWith("opp-123", "");
    });
  });
});
