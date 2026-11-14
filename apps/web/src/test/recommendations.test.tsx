import { describe, it, expect, vi } from "vitest";
import React from "react";
import { render, screen } from "@testing-library/react";
import RecommendationsPage from "@/app/(app)/recommendations/page";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn() }),
}));

vi.mock("@/lib/auth-context", () => ({
  useAuth: () => ({
    user: { id: "user-1", email: "test@nexus.io" },
    loading: false,
  }),
}));

vi.mock("@/lib/api", () => ({
  api: {
    matching: {
      getRecommendations: vi.fn().mockResolvedValue({
        content: [
          {
            id: "match-1",
            opportunityId: "opp-101",
            opportunity: {
              id: "opp-101",
              title: "Autonomous Robotics Fellowship",
              organization: "RoboFoundry",
              category: "Fellowships",
              remoteAvailable: true,
              deadline: "2027-06-01",
              status: "OPEN",
            },
            overallScore: 92.4,
            dimensionScores: {
              skills: 95.0,
              eligibility: 100.0,
              interest: 85.0,
              experience: 90.0,
              deadline: 90.0,
              location: 100.0,
            },
            eligibility: {
              status: "ELIGIBLE",
              reason: "Degree and enrollment criteria satisfied",
              missingProfileFields: [],
              criteriaChecked: ["Education level"],
            },
            matchedSkills: ["Python", "ROS"],
            missingRequiredSkills: [],
            missingPreferredSkills: ["Gazebo"],
            positiveFactors: ["Strong technical match"],
            negativeFactors: [],
            recommendationReason: "Strong Match: Your technical skills closely align.",
            algorithmVersion: "v1.0",
            calculatedAt: new Date().toISOString(),
          },
        ],
        totalElements: 1,
        totalPages: 1,
        size: 30,
        number: 0,
        empty: false,
        first: true,
        last: true,
      }),
      refreshMatches: vi.fn().mockResolvedValue({ refreshedCount: 1 }),
    },
  },
}));

describe("Personalized Recommendations Page", () => {
  it("renders recommendations feed with match scores, dimensions, and eligibility", async () => {
    render(<RecommendationsPage />);

    expect(await screen.findByText(/Personalized Recommendations/i)).toBeInTheDocument();
    expect(await screen.findByText("Autonomous Robotics Fellowship")).toBeInTheDocument();
    expect(screen.getByText("RoboFoundry")).toBeInTheDocument();
    expect(screen.getByText("92%")).toBeInTheDocument();
    expect(screen.getByText("Eligible")).toBeInTheDocument();
    expect(screen.getByText(/Strong Match: Your technical skills closely align/)).toBeInTheDocument();
  });
});
