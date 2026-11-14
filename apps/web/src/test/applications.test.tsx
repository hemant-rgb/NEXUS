import { describe, it, expect, vi, beforeEach } from "vitest";
import React from "react";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import ApplicationsPage from "@/app/(app)/applications/page";
import ApplicationDetailPage from "@/app/(app)/applications/[id]/page";
import { api } from "@/lib/api";
import {
  Application,
  ApplicationAnalytics,
  ApplicationActivity,
  ApplicationDocument,
  ApplicationInterview,
} from "@/lib/types";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn() }),
  useParams: () => ({ id: "app-101" }),
  useSearchParams: () => new URLSearchParams(),
}));

vi.mock("@/lib/api", () => ({
  api: {
    applications: {
      list: vi.fn(),
      get: vi.fn(),
      getAnalytics: vi.fn(),
      getActivities: vi.fn(),
      getDocuments: vi.fn(),
      getInterviews: vi.fn(),
      update: vi.fn(),
      delete: vi.fn(),
      generateDocument: vi.fn(),
      scheduleInterview: vi.fn(),
      answerQuestion: vi.fn(),
      createActivity: vi.fn(),
    },
  },
}));

describe("NEXUS Application Pipeline & Execution UI (Phase 7)", () => {
  const mockAnalytics: ApplicationAnalytics = {
    totalApplications: 6,
    preparingCount: 2,
    appliedCount: 2,
    inProgressCount: 1,
    interviewCount: 1,
    offerCount: 0,
    rejectedCount: 0,
    interviewRate: 33.3,
    offerRate: 0.0,
    upcomingDeadlinesCount: 3,
    statusBreakdown: { PREPARING: 2, APPLIED: 2, ASSESSMENT: 1, INTERVIEW: 1 },
  };

  const mockApp: Application = {
    id: "app-101",
    userId: "user-1",
    title: "Distributed Systems Intern",
    organization: "CockroachDB",
    applicationType: "INTERNSHIP",
    applicationUrl: "https://cockroachlabs.com/careers",
    status: "INTERVIEW",
    priority: "HIGH",
    deadline: "2026-12-01",
    appliedAt: "2026-11-10T12:00:00Z",
    userNotes: "Target storage layer team",
    activitiesCount: 3,
    documentsCount: 1,
    interviewsCount: 1,
    createdAt: "2026-11-10T10:00:00Z",
    updatedAt: "2026-11-14T10:00:00Z",
  };

  const mockDoc: ApplicationDocument = {
    id: "doc-1",
    applicationId: "app-101",
    documentType: "COVER_LETTER",
    title: "Cover Letter - CockroachDB",
    content: "Dear CockroachDB Hiring Team,\n\nI am writing to express my interest...",
    version: "v1.0",
    createdAt: "2026-11-10T10:30:00Z",
    updatedAt: "2026-11-10T10:30:00Z",
  };

  const mockInterview: ApplicationInterview = {
    id: "int-1",
    applicationId: "app-101",
    stageType: "TECHNICAL_INTERVIEW",
    scheduledAt: "2026-11-20T15:00:00Z",
    durationMinutes: 60,
    interviewerInfo: "Lead Storage Engineer",
    meetingLink: "https://meet.google.com/xyz",
    preparationNotes: "Review Raft consensus and LSM trees",
    status: "SCHEDULED",
    createdAt: "2026-11-12T10:00:00Z",
    updatedAt: "2026-11-12T10:00:00Z",
  };

  const mockActivity: ApplicationActivity = {
    id: "act-1",
    applicationId: "app-101",
    activityType: "STATUS_CHANGED",
    fromStatus: "APPLIED",
    toStatus: "INTERVIEW",
    description: "Status transitioned from APPLIED to INTERVIEW",
    createdAt: "2026-11-12T10:00:00Z",
  };

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(api.applications.list).mockResolvedValue([mockApp]);
    vi.mocked(api.applications.getAnalytics).mockResolvedValue(mockAnalytics);
    vi.mocked(api.applications.get).mockResolvedValue(mockApp);
    vi.mocked(api.applications.getActivities).mockResolvedValue([mockActivity]);
    vi.mocked(api.applications.getDocuments).mockResolvedValue([mockDoc]);
    vi.mocked(api.applications.getInterviews).mockResolvedValue([mockInterview]);
  });

  it("renders applications pipeline with analytics banner and kanban columns", async () => {
    render(<ApplicationsPage />);

    await waitFor(() => {
      expect(screen.getByText("Application Pipeline & Execution")).toBeDefined();
      expect(screen.getByText("33.3%")).toBeDefined();
      expect(screen.getByText("CockroachDB")).toBeDefined();
      expect(screen.getByText("Distributed Systems Intern")).toBeDefined();
    });
  });

  it("renders application detail workspace with tabs and document preview", async () => {
    render(<ApplicationDetailPage />);

    await waitFor(() => {
      expect(screen.getAllByText(/CockroachDB/i).length).toBeGreaterThan(0);
      expect(screen.getByRole("heading", { name: /Distributed Systems Intern/i })).toBeDefined();
      expect(screen.getByText("Documents & Proposals (1)")).toBeDefined();
      expect(screen.getByText("Interviews & Rounds (1)")).toBeDefined();
    });
  });

  it("allows switching to Documents tab to view generated cover letter", async () => {
    render(<ApplicationDetailPage />);

    await waitFor(() => {
      expect(screen.getByText("Documents & Proposals (1)")).toBeDefined();
    });

    const docTab = screen.getByText("Documents & Proposals (1)");
    fireEvent.click(docTab);

    await waitFor(() => {
      expect(screen.getAllByText(/Cover Letter - CockroachDB/i).length).toBeGreaterThan(0);
      expect(screen.getByText(/Dear CockroachDB Hiring Team/)).toBeDefined();
    });
  });
});
