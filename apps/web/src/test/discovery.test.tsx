import { describe, it, expect, vi } from "vitest";
import React from "react";
import { render, screen } from "@testing-library/react";
import OpportunitiesPage from "../app/(app)/opportunities/page";

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
      list: vi.fn().mockResolvedValue({
        content: [
          {
            id: "opp-1",
            title: "Kernel Mentorship",
            organization: "Linux Foundation",
            description: "Linux kernel work",
            category: "Open-source programs",
            opportunityType: "OPEN_SOURCE",
            remoteAvailable: true,
            requiredSkills: ["C", "Linux"],
            preferredSkills: [],
            applicationUrl: "https://lfx.org",
            deadline: "2027-05-01",
            deadlineType: "STRICT",
            status: "OPEN",
            createdAt: new Date().toISOString(),
            lastVerifiedAt: new Date().toISOString(),
            isSaved: false,
          },
        ],
        totalElements: 1,
        totalPages: 1,
        size: 12,
        number: 0,
        empty: false,
        first: true,
        last: true,
      }),
      save: vi.fn(),
      removeSaved: vi.fn(),
    },
  },
}));

describe("Opportunities Discovery Page", () => {
  it("renders opportunity feed with search bar and fetched opportunity cards", async () => {
    render(<OpportunitiesPage />);
    expect(await screen.findByText(/Opportunity Discovery/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Search opportunities/i)).toBeInTheDocument();
    expect(await screen.findByText("Kernel Mentorship")).toBeInTheDocument();
    expect(screen.getByText("Linux Foundation")).toBeInTheDocument();
  });
});
