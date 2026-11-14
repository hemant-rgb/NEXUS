# NEXUS — Phase 9: Autonomous AI Agents & Workflow Orchestration Report

## 1. Executive Summary
Phase 9 delivers **NEXUS Autonomous Agents**, an intelligent, controlled workflow orchestration engine. NEXUS introduces a specialized roster of domain agents, a strictly-whitelisted tool registry, auditable multi-step execution traces, and mandatory human-in-the-loop safety gating (Level 3 Autonomy) for external or irreversible operations.

---

## 2. Autonomy Model & Safety Boundaries

NEXUS strictly follows a 4-tier autonomy governance model:

| Level | Classification | Behavior & Authorization |
|---|---|---|
| **Level 0** | **Read-Only Analysis** | Autonomous execution of read-only queries and intelligence evaluations. No state mutation. |
| **Level 1** | **Drafting & Planning** | Autonomous creation of drafts, proposals, roadmap revisions, and plans. Persisted as reviewable drafts. |
| **Level 2** | **Reversible Internal Actions** | Autonomous internal mutations (e.g. setting task reminders, updating internal status tags). Easily reversible. |
| **Level 3** | **External / Irreversible Actions** | **MANDATORY HUMAN APPROVAL GATING**. Agent execution automatically pauses (`WAITING_APPROVAL`), publishes an `AgentApprovalRequiredEvent`, creates an approval record, and requires explicit human confirmation before resuming. |

> **Safety Commitment**: NEXUS agents never access unrestricted shell or raw browser automation, and never execute public code pushing or external job application submission without explicit user authorization.

---

## 3. Specialized Agent Roster

1. **`ScoutAgent` (`SCOUT`)**:
   - *Autonomy*: Level 0 (Read-Only)
   - *Permitted Tools*: `SEARCH_OPPORTUNITIES`, `GET_USER_PROFILE`, `GET_USER_SKILLS`
   - *Purpose*: Discovers relevant opportunities matching user profile, skills, and target career interests.

2. **`ResearchAgent` (`RESEARCH`)**:
   - *Autonomy*: Level 0 (Read-Only)
   - *Permitted Tools*: `GET_OPPORTUNITY`, `GET_MATCH_ANALYSIS`
   - *Purpose*: Investigates opportunity requirements, organizational background, and evaluation criteria.

3. **`StrategyAgent` (`STRATEGY`)**:
   - *Autonomy*: Level 1 (Drafting & Planning)
   - *Permitted Tools*: `GET_MATCH_ANALYSIS`, `GET_ROADMAP`, `GET_USER_SKILLS`
   - *Purpose*: Formulates personalized preparation milestones and learning plans based on 6D match gaps.

4. **`GitHubAgent` (`GITHUB`)**:
   - *Autonomy*: Level 1 (Drafting & Planning)
   - *Permitted Tools*: `GET_GITHUB_ISSUES`, `GET_USER_SKILLS`
   - *Purpose*: Discovers suitable open-source issues and drafts structured contribution implementation roadmaps.

5. **`ApplicationAgent` (`APPLICATION`)**:
   - *Autonomy*: Level 1 (Drafting) / Level 3 (Safety Gated)
   - *Permitted Tools*: `GET_OPPORTUNITY`, `DRAFT_APPLICATION_DOCUMENT`, `GET_USER_PROFILE`
   - *Purpose*: Prepares tailored application responses, draft cover letters, and resumes. If external submission is requested, automatically triggers Level 3 Safety Gating.

6. **`MonitoringAgent` (`MONITORING`)**:
   - *Autonomy*: Level 2 (Reversible Internal Actions)
   - *Permitted Tools*: `GET_ROADMAP`, `CREATE_NOTIFICATION`
   - *Purpose*: Interprets monitoring events, audits stalled milestones, and creates prioritized action notifications.

---

## 4. Controlled Tool Registry

All agent capabilities are encapsulated inside registered `AgentTool` instances:
- `SearchOpportunitiesTool`: Controlled search over vetted opportunities.
- `GetOpportunityTool`: Detail retrieval by ID.
- `GetUserProfileTool` & `GetSkillsTool`: Read-only access to user profile and skills.
- `GetMatchScoreTool`: Deterministic 6D scoring evaluation.
- `GetRoadmapTool`: Inspection of preparation milestones and tasks.
- `GetGitHubIssuesTool`: Vetted open-source issue retrieval.
- `DraftDocumentTool`: Tailored drafting of application documents and proposals.
- `CreateNotificationTool`: Internal alert emission through the Pulse notification service.

Agents cannot invoke unregistered tools or perform reflection-based execution.

---

## 5. Database Schema (Flyway V11 Migration)

Migration file: `V11__nexus_autonomous_agents.sql`

### 1. `agent_runs`
- `id` (UUID, Primary Key)
- `user_id` (UUID, Foreign Key to `users.id` with CASCADE delete)
- `agent_id` (VARCHAR(64), e.g. `SCOUT`, `APPLICATION`)
- `status` (VARCHAR(32), Enum: `PENDING`, `RUNNING`, `WAITING_APPROVAL`, `COMPLETED`, `FAILED`, `CANCELLED`)
- `input_summary` (VARCHAR(255))
- `input_context` (TEXT, JSON string)
- `output_summary` (TEXT)
- `output_result` (TEXT, JSON string)
- `error_summary` (TEXT)
- `token_usage` (INT)
- `estimated_cost` (NUMERIC(8,4))
- `started_at`, `completed_at`, `created_at` (TIMESTAMPTZ)
- Indexes: `idx_agent_runs_user`, `idx_agent_runs_status`

### 2. `agent_steps`
- `id` (UUID, Primary Key)
- `agent_run_id` (UUID, Foreign Key to `agent_runs.id` with CASCADE delete)
- `sequence_order` (INT)
- `step_type` (VARCHAR(32), Enum: `THINKING`, `TOOL_CALL`, `DECISION`, `DRAFTING`, `VERIFICATION`)
- `tool_id` (VARCHAR(64))
- `input_summary` (TEXT)
- `output_summary` (TEXT)
- `status` (VARCHAR(32), Enum: `STARTED`, `COMPLETED`, `FAILED`, `SKIPPED`)
- `started_at`, `completed_at` (TIMESTAMPTZ)
- Indexes: `idx_agent_steps_run`

### 3. `agent_approvals`
- `id` (UUID, Primary Key)
- `agent_run_id` (UUID, Foreign Key to `agent_runs.id` with CASCADE delete)
- `user_id` (UUID, Foreign Key to `users.id`)
- `action_type` (VARCHAR(64), e.g. `SUBMIT_APPLICATION`)
- `description` (TEXT)
- `action_payload` (TEXT)
- `status` (VARCHAR(32), Enum: `PENDING`, `APPROVED`, `REJECTED`)
- `reviewer_decision_notes` (TEXT)
- `created_at`, `decided_at` (TIMESTAMPTZ)
- Indexes: `idx_agent_approvals_user`, `idx_agent_approvals_status`

---

## 6. REST API Specification (`/api/v1`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/agents` | List all specialized agents, descriptions, autonomy levels & permitted tools |
| `GET` | `/agents/{agentId}` | Get single agent metadata and parameter specification |
| `POST` | `/agents/{agentId}/runs` | Deploy agent run with goal and contextual parameters |
| `GET` | `/users/me/agent-runs` | Paginated history of user's agent runs |
| `GET` | `/agent-runs/{runId}` | Get run details, status, token usage, and executive summary |
| `POST` | `/agent-runs/{runId}/cancel` | Cancel in-progress or paused agent run |
| `GET` | `/agent-runs/{runId}/steps` | Fetch multi-step chronological execution trace |
| `GET` | `/users/me/agent-approvals` | Fetch pending or historical Level 3 approval requests |
| `POST` | `/agent-approvals/{id}/approve` | Authorize paused Level 3 action and resume agent workflow |
| `POST` | `/agent-approvals/{id}/reject` | Reject Level 3 action with decision notes |

---

## 7. Frontend Autonomous Agent Workspace (`/agents`)
- **Specialized Agent Roster**: Card grid displaying agent name, description, autonomy badge, and permitted tools tags.
- **Deploy Form**: Agent goal input, optional target opportunity/application UUID, and Level 3 External Submission safety checkbox.
- **Execution Trace Terminal**: Step-by-step telemetry cards displaying step type (`THINKING`, `TOOL_CALL`, `DRAFTING`, `VERIFICATION`), tool name, inputs, outputs, and timestamps.
- **Level 3 Safety Pause Banner**: Interactive banner presenting proposed action details, target payload, and explicit "Authorize Action" or "Reject" buttons.
- **Approvals & Runs Tabs**: Dedicated tabs for managing approval queues and historical agent execution telemetry.

---

## 8. Verification Matrix
- **Flyway Database Migration**: Applied successfully to `V11` in PostgreSQL 16.
- **Backend Test Suite**: `AgentOrchestrationServiceTest` (5 tests), `AgentApprovalServiceTest` (4 tests) — 100% pass rate. Total backend suite: 101 tests passed with 0 failures.
- **Frontend Test Suite**: `agents.test.tsx` (2 tests in Vitest) — 100% pass rate. Total frontend suite: 30 tests passed with 0 failures.
- **End-to-End Test**: `e2e/pulse-and-agents.spec.ts` (Journey I: NEXUS Autonomous Agents) passed in Playwright (2.1s).
- **Regression Suite**: All 9 Playwright end-to-end user journeys (Phases 1–9) passed with 100% success in 25.7s.
