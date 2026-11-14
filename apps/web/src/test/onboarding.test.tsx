import { describe, it, expect, vi } from "vitest";
import React from "react";
import { render, screen } from "@testing-library/react";
import OnboardingPage from "../app/(app)/onboarding/page";

// Mock router and auth-context
vi.mock("next/navigation", () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
  }),
}));

vi.mock("@/lib/auth-context", () => ({
  useAuth: () => ({
    user: { fullName: "Test Developer", email: "test@nexus.io" },
    loading: false,
  }),
}));

vi.mock("@/lib/api", () => ({
  api: {
    profile: {
      get: vi.fn().mockResolvedValue({
        fullName: "Test Developer",
        countryCode: "US",
        timezone: "UTC",
        institution: "Test University",
        onboardingStep: 1,
        onboardingCompleted: false,
      }),
      update: vi.fn().mockResolvedValue({}),
    },
    preferences: {
      get: vi.fn().mockResolvedValue({
        interests: ["Software Engineering"],
        preferredCategories: ["Internships"],
        remotePreference: "REMOTE",
        preferredCountries: ["US"],
        weeklyHours: 15,
      }),
      update: vi.fn().mockResolvedValue({}),
    },
    skills: {
      getUserSkills: vi.fn().mockResolvedValue([]),
      catalog: vi.fn().mockResolvedValue([
        { id: "1", name: "TypeScript", category: "Languages" },
        { id: "2", name: "Java", category: "Languages" },
      ]),
      addUserSkill: vi.fn(),
      deleteUserSkill: vi.fn(),
    },
  },
}));

describe("Onboarding Page", () => {
  it("renders onboarding wizard step 1 correctly", async () => {
    render(<OnboardingPage />);
    expect(await screen.findByText(/Profile Onboarding/i)).toBeInTheDocument();
    expect(screen.getByText(/Personal & Geographic Information/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Continue/i })).toBeInTheDocument();
  });
});
