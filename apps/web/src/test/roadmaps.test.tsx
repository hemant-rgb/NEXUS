import { describe, it, expect, vi, beforeEach } from "vitest";
import React from "react";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import RoadmapsPage from "@/app/(app)/roadmaps/page";
import RoadmapDetailPage from "@/app/(app)/roadmaps/[id]/page";
import { api } from "@/lib/api";
import { Roadmap, RoadmapSummary } from "@/lib/types";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn() }),
  useParams: () => ({ id: "roadmap-101" }),
  useSearchParams: () => new URLSearchParams(),
}));

vi.mock("@/lib/auth-context", () => ({
  useAuth: () => ({
    user: { id: "user-1", email: "test@nexus.io" },
    loading: false,
  }),
}));

vi.mock("@/lib/api", () => ({
  api: {
    roadmaps: {
      list: vi.fn(),
      get: vi.fn(),
      generate: vi.fn(),
      updateTask: vi.fn(),
      reschedule: vi.fn(),
      regenerate: vi.fn(),
      delete: vi.fn(),
      addTaskResource: vi.fn(),
    },
  },
}));

describe("NEXUS Strategy: Preparation Roadmaps UI", () => {
  const mockRoadmapSummary: RoadmapSummary = {
    id: "roadmap-101",
    title: "Google Summer of Code 2027 Mastery",
    description: "Preparation path for GSoC",
    roadmapType: "OPPORTUNITY_PREPARATION",
    targetOpportunityId: "opp-123",
    targetOpportunityTitle: "Google Summer of Code 2027 Contributor",
    status: "ACTIVE",
    startDate: "2027-03-01",
    targetCompletionDate: "2027-04-15",
    estimatedDurationWeeks: 6,
    availableHoursPerWeek: 12,
    overallProgressPercentage: 40.0,
    totalMilestones: 4,
    completedMilestones: 1,
    totalTasks: 8,
    completedTasks: 3,
    createdAt: "2027-02-01T00:00:00Z",
    updatedAt: "2027-02-15T00:00:00Z",
  };

  const mockDetailRoadmap: Roadmap = {
    id: "roadmap-101",
    userId: "user-1",
    title: "Google Summer of Code 2027 Mastery",
    description: "Preparation path for GSoC",
    roadmapType: "OPPORTUNITY_PREPARATION",
    targetOpportunityId: "opp-123",
    targetOpportunityTitle: "Google Summer of Code 2027 Contributor",
    status: "ACTIVE",
    startDate: "2027-03-01",
    targetCompletionDate: "2027-04-15",
    estimatedDurationWeeks: 6,
    availableHoursPerWeek: 12,
    generationVersion: "v1.0",
    overallProgressPercentage: 50.0,
    totalMilestones: 2,
    completedMilestones: 1,
    totalTasks: 2,
    completedTasks: 1,
    remainingEstimatedHours: 4.0,
    milestones: [
      {
        id: "m-1",
        roadmapId: "roadmap-101",
        title: "Core Foundations & Tooling",
        description: "Master prerequisites",
        sequenceOrder: 1,
        estimatedDurationWeeks: 1,
        targetDate: "2027-03-07",
        status: "COMPLETED",
        completionPercentage: 100.0,
        tasks: [
          {
            id: "t-1",
            milestoneId: "m-1",
            roadmapId: "roadmap-101",
            title: "Setup Local Docker Sandbox",
            description: "Install docker and dependencies",
            completionCriteria: "Container boots and tests pass",
            taskType: "PRACTICE",
            priority: "HIGH",
            estimatedHours: 2.0,
            sequenceOrder: 1,
            scheduledDate: "2027-03-02",
            status: "COMPLETED",
            resources: [],
          },
        ],
      },
      {
        id: "m-2",
        roadmapId: "roadmap-101",
        title: "Capstone Deliverable",
        description: "Build working project",
        sequenceOrder: 2,
        estimatedDurationWeeks: 2,
        targetDate: "2027-03-21",
        status: "IN_PROGRESS",
        completionPercentage: 0.0,
        tasks: [
          {
            id: "t-2",
            milestoneId: "m-2",
            roadmapId: "roadmap-101",
            title: "Implement REST Service Endpoints",
            description: "Build API with validation and tests",
            completionCriteria: "Endpoints pass integration tests",
            taskType: "PROJECT",
            priority: "HIGH",
            estimatedHours: 4.0,
            sequenceOrder: 1,
            scheduledDate: "2027-03-15",
            status: "NOT_STARTED",
            resources: [
              {
                id: "tr-1",
                taskId: "t-2",
                title: "Spring Boot REST Guide",
                url: "https://spring.io/guides",
                provider: "Spring Official",
                resourceType: "TUTORIAL",
                verificationStatus: "VERIFIED",
              },
            ],
          },
        ],
      },
    ],
    createdAt: "2027-02-01T00:00:00Z",
    updatedAt: "2027-02-15T00:00:00Z",
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders roadmaps dashboard with statistics and active roadmaps", async () => {
    vi.mocked(api.roadmaps.list).mockResolvedValueOnce([mockRoadmapSummary]);

    render(<RoadmapsPage />);

    expect(screen.getByText(/Personalized Preparation Roadmaps/i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText("Google Summer of Code 2027 Mastery")).toBeInTheDocument();
      expect(screen.getByText(/Target: Google Summer of Code 2027 Contributor/i)).toBeInTheDocument();
      expect(screen.getByText(/40%/)).toBeInTheDocument();
      expect(screen.getByText(/12h\/wk/)).toBeInTheDocument();
    });
  });

  it("renders empty state when no roadmaps exist", async () => {
    vi.mocked(api.roadmaps.list).mockResolvedValueOnce([]);

    render(<RoadmapsPage />);

    await waitFor(() => {
      expect(screen.getByText(/No Preparation Roadmaps Found/i)).toBeInTheDocument();
      expect(screen.getByText(/Prepare for an Opportunity/i)).toBeInTheDocument();
      expect(screen.getByText(/Set a Career Goal/i)).toBeInTheDocument();
    });
  });

  it("renders roadmap detail with milestones, tasks, and completion criteria", async () => {
    vi.mocked(api.roadmaps.get).mockResolvedValueOnce(mockDetailRoadmap);

    render(<RoadmapDetailPage />);

    await waitFor(() => {
      expect(screen.getByText("Google Summer of Code 2027 Mastery")).toBeInTheDocument();
      expect(screen.getByText("Core Foundations & Tooling")).toBeInTheDocument();
      expect(screen.getByText("Capstone Deliverable")).toBeInTheDocument();
      expect(screen.getByText("Setup Local Docker Sandbox")).toBeInTheDocument();
      expect(screen.getByText("Implement REST Service Endpoints")).toBeInTheDocument();
    });
  });

  it("allows user to toggle task completion status", async () => {
    vi.mocked(api.roadmaps.get).mockResolvedValue(mockDetailRoadmap);
    vi.mocked(api.roadmaps.updateTask).mockResolvedValueOnce({
      ...mockDetailRoadmap.milestones[1].tasks[0],
      status: "COMPLETED",
    });

    render(<RoadmapDetailPage />);

    await waitFor(() => {
      expect(screen.getByText("Implement REST Service Endpoints")).toBeInTheDocument();
    });

    const toggleBtns = screen.getAllByTitle(/Mark Complete|Mark Incomplete/i);
    expect(toggleBtns.length).toBeGreaterThan(0);

    fireEvent.click(toggleBtns[1]);

    await waitFor(() => {
      expect(api.roadmaps.updateTask).toHaveBeenCalledWith(
        "roadmap-101",
        "t-2",
        { status: "COMPLETED" }
      );
    });
  });
});
