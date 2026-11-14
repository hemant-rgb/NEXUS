import React from "react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import AgentsWorkspacePage from "@/app/(app)/agents/page";
import { api } from "@/lib/api";

vi.mock("@/lib/api", () => ({
  api: {
    agents: {
      list: vi.fn(),
      getUserRuns: vi.fn(),
      getApprovals: vi.fn(),
      startRun: vi.fn(),
      approve: vi.fn(),
      reject: vi.fn(),
    },
  },
}));

describe("NEXUS Autonomous Agents UI (Phase 9)", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (api.agents.list as any).mockResolvedValue([
      {
        agentId: "SCOUT",
        name: "NEXUS Scout Agent",
        description: "Discovers relevant opportunities matching user profile and skills",
        autonomyLevel: 0,
        autonomyDescription: "Read-Only Analysis",
        permittedTools: ["SEARCH_OPPORTUNITIES", "GET_USER_PROFILE"],
      },
      {
        agentId: "APPLICATION",
        name: "NEXUS Application Agent",
        description: "Prepares tailored application materials",
        autonomyLevel: 1,
        autonomyDescription: "Drafting & Planning",
        permittedTools: ["DRAFT_APPLICATION_DOCUMENT"],
      },
    ]);
    (api.agents.getUserRuns as any).mockResolvedValue({
      content: [
        {
          id: "run-1",
          agentId: "SCOUT",
          status: "COMPLETED",
          triggerType: "MANUAL",
          inputSummary: "Find 5 software internships",
          outputSummary: "Scout discovery completed successfully",
          tokenUsage: 150,
          estimatedCost: 0.0003,
          createdAt: new Date().toISOString(),
          steps: [
            {
              id: "step-1",
              sequenceOrder: 1,
              stepType: "THINKING",
              status: "COMPLETED",
              outputSummary: "Analyzing user profile criteria...",
              startedAt: new Date().toISOString(),
            },
          ],
          approvals: [],
        },
      ],
    });
    (api.agents.getApprovals as any).mockResolvedValue([]);
  });

  it("renders specialized agent roster and active execution trace", async () => {
    render(<AgentsWorkspacePage />);

    await waitFor(() => {
      expect(screen.getByText(/NEXUS Autonomous Agents/i)).toBeInTheDocument();
      expect(screen.getAllByText(/NEXUS Scout Agent/i)[0]).toBeInTheDocument();
      expect(screen.getByText(/Scout discovery completed successfully/i)).toBeInTheDocument();
    });
  });

  it("allows selecting an agent and submitting a task", async () => {
    (api.agents.startRun as any).mockResolvedValue({
      id: "run-2",
      agentId: "SCOUT",
      status: "COMPLETED",
      triggerType: "MANUAL",
      inputSummary: "Find React opportunities",
      outputSummary: "Found 3 React opportunities",
      tokenUsage: 120,
      estimatedCost: 0.0002,
      createdAt: new Date().toISOString(),
      steps: [],
      approvals: [],
    });

    render(<AgentsWorkspacePage />);
    await waitFor(() => expect(screen.getAllByText(/NEXUS Scout Agent/i)[0]).toBeInTheDocument());

    const input = screen.getByPlaceholderText(/Find 5 high-match/i);
    fireEvent.change(input, { target: { value: "Find React opportunities" } });

    const deployBtn = screen.getByRole("button", { name: /Deploy SCOUT/i });
    fireEvent.click(deployBtn);

    await waitFor(() => {
      expect(api.agents.startRun).toHaveBeenCalledWith("SCOUT", {
        goal: "Find React opportunities",
        parameters: {},
      });
    });
  });
});
