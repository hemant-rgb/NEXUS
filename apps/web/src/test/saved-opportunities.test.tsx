import { describe, it, expect, vi } from "vitest";
import React from "react";
import { render, screen } from "@testing-library/react";
import SavedOpportunitiesPage from "../app/(app)/saved/page";

vi.mock("next/navigation", () => ({
  useRouter: () => ({
    push: vi.fn(),
  }),
}));

vi.mock("@/lib/auth-context", () => ({
  useAuth: () => ({
    user: { id: "user-1", email: "test@nexus.io" },
    loading: false,
  }),
}));

vi.mock("@/lib/api", () => ({
  api: {
    opportunities: {
      saved: vi.fn().mockResolvedValue([
        {
          id: "saved-1",
          opportunityId: "opp-1",
          notes: "Need to prepare technical pitch",
          savedAt: new Date().toISOString(),
          opportunity: {
            id: "opp-1",
            title: "Distributed Systems SWE",
            organization: "CloudScale",
            description: "Distributed storage engineering",
            category: "Internships",
            opportunityType: "INTERNSHIP",
            remoteAvailable: true,
            requiredSkills: ["Go"],
            preferredSkills: [],
            applicationUrl: "https://cloudscale.io",
            deadline: "2027-03-01",
            deadlineType: "ROLLING",
            status: "OPEN",
            createdAt: new Date().toISOString(),
            lastVerifiedAt: new Date().toISOString(),
            isSaved: true,
          },
        },
      ]),
      removeSaved: vi.fn(),
      updateNotes: vi.fn(),
    },
  },
}));

describe("Saved Opportunities Page", () => {
  it("renders saved opportunities and shows personal notes", async () => {
    render(<SavedOpportunitiesPage />);
    expect(await screen.findByText(/Saved Opportunities/i)).toBeInTheDocument();
    expect(await screen.findByText("Distributed Systems SWE")).toBeInTheDocument();
    expect(screen.getByText("Need to prepare technical pitch")).toBeInTheDocument();
  });
});
