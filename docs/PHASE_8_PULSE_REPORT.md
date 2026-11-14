# NEXUS — Phase 8: NEXUS Pulse Architecture & Verification Report

## 1. Executive Summary
Phase 8 introduces **NEXUS Pulse**, an intelligent monitoring, event-driven notification, and deadline-tracking subsystem. Rather than serving solely as a passive query system, NEXUS actively tracks user deadlines, milestone drift, application statuses, interview schedules, and open-source issue changes, transforming intelligence into timely, actionable notifications.

---

## 2. Architectural Boundaries & Domain Event Model

Pulse is decoupled from transactional business domains through Spring's Application Event infrastructure:
- **`NexusEventPublisher`**: Uniform interface for publishing domain events across all application modules.
- **`NexusDomainEvent`**: Strongly-typed base abstraction carrying `userId`, `timestamp`, and `aggregateId`.
- **`NexusNotificationEventListener`**: Asynchronous event handler that translates domain events into structured notifications with idempotency and quiet-hours filtering.

### Domain Events Supported:
1. `OpportunityDeadlineApproachingEvent` (`OPPORTUNITY`): Triggered when saved opportunity closing dates are within 3 days or 24 hours.
2. `OpportunityStatusChangedEvent` (`OPPORTUNITY`): Triggered when opportunity state transitions (e.g., active -> closed).
3. `ApplicationStatusChangedEvent` (`APPLICATION`): Triggered on application stage transitions.
4. `InterviewScheduledEvent` (`APPLICATION`): Triggered on upcoming interview rounds.
5. `RoadmapTaskDueEvent` / `RoadmapTaskOverdueEvent` (`ROADMAP`): Triggered when preparation roadmap tasks are due or overdue.
6. `GitHubIssueUpdatedEvent` (`GITHUB`): Triggered when tracked issues or contribution opportunities are modified.
7. `AgentApprovalRequiredEvent` (`AGENT`): Level 3 safety pause alerts requiring human authorization.
8. `AgentRunCompletedEvent` / `AgentRunFailedEvent` (`AGENT`): Autonomous workflow completion/failure telemetry.

---

## 3. Database Schema (Flyway V10 Migration)

Migration file: `V10__nexus_pulse_notifications_and_monitoring.sql`

### 1. `notifications`
- `id` (UUID, Primary Key)
- `user_id` (UUID, Foreign Key to `users.id` with CASCADE delete)
- `category` (VARCHAR(32), Enum: `OPPORTUNITY`, `APPLICATION`, `ROADMAP`, `GITHUB`, `AGENT`, `SYSTEM`)
- `priority` (VARCHAR(16), Enum: `LOW`, `NORMAL`, `HIGH`, `CRITICAL`)
- `status` (VARCHAR(16), Enum: `UNREAD`, `READ`, `ARCHIVED`)
- `title` (VARCHAR(255))
- `message` (TEXT)
- `action_url` (VARCHAR(512))
- `idempotency_key` (VARCHAR(128)) - Guarantees duplicate alerts are not generated
- `metadata` (JSONB)
- `expires_at` (TIMESTAMPTZ)
- `created_at` (TIMESTAMPTZ)
- Indexes: `idx_notifications_user_status`, `idx_notifications_user_category`, `idx_notifications_idempotency`

### 2. `notification_preferences`
- `id` (UUID, Primary Key)
- `user_id` (UUID, Unique Foreign Key to `users.id`)
- `in_app_enabled` (BOOLEAN, default true)
- `email_enabled` (BOOLEAN, default false)
- `opportunity_alerts`, `application_alerts`, `roadmap_alerts`, `github_alerts`, `agent_alerts` (BOOLEAN, default true)
- `frequency` (VARCHAR(32), default 'IMMEDIATE')
- `quiet_hours_enabled` (BOOLEAN, default false)
- `quiet_hours_start` (VARCHAR(8), '22:00')
- `quiet_hours_end` (VARCHAR(8), '08:00')
- `timezone` (VARCHAR(64), 'UTC')

### 3. `notification_deliveries`
- `id` (UUID, Primary Key)
- `notification_id` (UUID, Foreign Key)
- `channel` (VARCHAR(32), Enum: `IN_APP`, `EMAIL`, `WEBHOOK`)
- `status` (VARCHAR(16), Enum: `PENDING`, `DELIVERED`, `FAILED`, `SUPPRESSED`)
- `attempt_count` (INT)
- `delivered_at` (TIMESTAMPTZ)
- `error_message` (TEXT)

---

## 4. Multi-Channel Routing & Quiet Hours Policy
1. **In-App Delivery**: Persisted immediately to PostgreSQL and surfaced in the top navigation badge and Pulse Activity Center.
2. **Email Delivery**: Pluggable `EmailDeliveryService` with `ConsoleEmailDeliveryService` for local development and mock delivery traces.
3. **Quiet Hours**: During user-configured quiet hours, non-`CRITICAL` alerts are marked `SUPPRESSED` from external push delivery while remaining visible in-app.
4. **Idempotency**: All automated scanners generate deterministic keys (e.g. `opp-deadline-{oppId}-{date}`) preventing duplicate spamming.

---

## 5. REST API Specification (`/api/v1`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/users/me/notifications` | Paginated notifications filtered by category & status |
| `GET` | `/users/me/notifications/unread-count` | Real-time unread alert count for navbar badge |
| `PATCH` | `/notifications/{id}/read` | Mark individual notification as read |
| `PATCH` | `/notifications/{id}/archive` | Archive notification from active inbox |
| `POST` | `/users/me/notifications/read-all` | Batch mark all notifications as read |
| `GET` | `/users/me/notification-preferences` | Fetch user notification channels & quiet hours |
| `PUT` | `/users/me/notification-preferences` | Update delivery channels and monitored categories |
| `GET` | `/users/me/activity` | Unified timeline telemetry stream |
| `POST` | `/monitoring/trigger` | Manually run full monitoring cycle scan |

All endpoints wrap payloads inside standard `ApiResponse<T>`.

---

## 6. Frontend Pulse Center (`/activity`)
- **Alerts & Notifications Tab**: Category filter chips, unread count pill, instant "Mark All Read" action, priority badges (`CRITICAL`, `HIGH`, `NORMAL`), contextual action buttons linking directly to target entities.
- **Telemetry Stream Tab**: Visual chronological audit trail showing deadline detections, state transitions, and background events.
- **Preferences Tab**: Real-time configuration form for in-app/email channels, quiet hours start/end, timezone, and individual subsystem notification toggles.
- **Top Navigation Real-time Badge**: Unread alert counter in `apps/web/src/components/shell/top-nav.tsx` showing current alert status.

---

## 7. Verification Matrix
- **Flyway Database Migration**: Applied successfully to `V10` in PostgreSQL 16.
- **Backend Unit Tests**: `NotificationServiceTest` (6 tests), `MonitoringServiceTest` (2 tests) — 100% pass rate.
- **Frontend Unit Tests**: `pulse.test.tsx` (2 tests in Vitest) — 100% pass rate.
- **End-to-End Test**: `e2e/pulse-and-agents.spec.ts` (Journey H: NEXUS Pulse) passed in Playwright (2.2s).
