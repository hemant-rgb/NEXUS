import React from "react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import ActivityPulsePage from "@/app/(app)/activity/page";
import { api } from "@/lib/api";

vi.mock("@/lib/api", () => ({
  api: {
    pulse: {
      getNotifications: vi.fn(),
      getUnreadCount: vi.fn(),
      getActivity: vi.fn(),
      getPreferences: vi.fn(),
      markAsRead: vi.fn(),
      archive: vi.fn(),
      markAllAsRead: vi.fn(),
      triggerMonitoring: vi.fn(),
      updatePreferences: vi.fn(),
    },
  },
}));

describe("NEXUS Pulse UI (Phase 8)", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (api.pulse.getNotifications as any).mockResolvedValue({
      content: [
        {
          id: "notif-1",
          notificationType: "OPPORTUNITY_DEADLINE_APPROACHING",
          category: "OPPORTUNITY",
          title: "Deadline Approaching: Stripe Backend Internship",
          message: "2 days remaining before the opportunity deadline.",
          priority: "HIGH",
          status: "UNREAD",
          createdAt: new Date().toISOString(),
          actionUrl: "/opportunities/123",
        },
      ],
    });
    (api.pulse.getUnreadCount as any).mockResolvedValue({ unreadCount: 1 });
    (api.pulse.getActivity as any).mockResolvedValue([]);
    (api.pulse.getPreferences as any).mockResolvedValue({
      id: "pref-1",
      inAppEnabled: true,
      emailEnabled: false,
      opportunityAlerts: true,
      applicationAlerts: true,
      roadmapAlerts: true,
      githubAlerts: true,
      agentAlerts: true,
      frequency: "IMMEDIATE",
      quietHoursEnabled: false,
      quietHoursStart: "22:00",
      quietHoursEnd: "08:00",
      timezone: "UTC",
    });
  });

  it("renders Pulse notifications with title, category, and unread badge", async () => {
    render(<ActivityPulsePage />);

    await waitFor(() => {
      expect(screen.getByText(/NEXUS Pulse/i)).toBeInTheDocument();
      expect(screen.getByText(/Deadline Approaching: Stripe Backend Internship/i)).toBeInTheDocument();
      expect(screen.getByText(/1 Unread/i)).toBeInTheDocument();
    });
  });

  it("triggers manual monitoring scan on button click", async () => {
    (api.pulse.triggerMonitoring as any).mockResolvedValue({
      opportunityDeadlinesDetected: 2,
      applicationDeadlinesDetected: 1,
      roadmapAlertsDetected: 0,
    });

    render(<ActivityPulsePage />);
    await waitFor(() => expect(screen.getByText(/NEXUS Pulse/i)).toBeInTheDocument());

    const scanBtn = screen.getByRole("button", { name: /Scan Monitoring/i });
    fireEvent.click(scanBtn);

    await waitFor(() => {
      expect(api.pulse.triggerMonitoring).toHaveBeenCalled();
      expect(screen.getByText(/Pulse cycle completed/i)).toBeInTheDocument();
    });
  });
});
