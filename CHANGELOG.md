# Changelog

All notable changes to the NEXUS platform are documented in this file.

## [1.0.0] - 2026-11-14 - Phase 10: Production Hardening, Deployment & Open-Source Launch

### Added
- Multi-stage production Dockerfiles for Spring Boot (`apps/backend/Dockerfile`) and Next.js (`apps/web/Dockerfile`).
- Full-stack self-hosting and production Docker Compose configurations (`docker-compose.yml`, `docker-compose.prod.yml`).
- Automated PostgreSQL backup (`backup-db.sh`) and restoration (`restore-db.sh`) scripts with retention pruning.
- Authentication brute-force rate limiter (`RateLimitingFilter`) with sliding-window algorithm and 429 status.
- Request correlation ID tracking (`CorrelationIdFilter`) with MDC structured logging.
- SSRF URL validator (`UrlSecurityValidator`) preventing loopback and private network access.
- Multi-provider AI abstraction support (`GeminiAiProvider`, `OpenAiAiProvider`, `LocalFallbackAiProvider`) with token cost estimation.
- Kubernetes-compatible Actuator liveness and readiness probes.
- Open-source governance files (`LICENSE`, `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, `SUPPORT.md`, issue/PR templates).

### Enhanced
- Graceful shutdown configuration for Spring Boot backend.
- Standalone Next.js optimization with container-aware backend rewrites.
- Production environment template (`.env.example`) with complete configuration references.

---

## [0.9.0] - 2026-11-14 - Phase 9: Autonomous AI Agents & Workflow Orchestration
- Flyway V11 migration: `agent_runs`, `agent_steps`, `agent_approvals`.
- Autonomous agent roster: `SCOUT`, `RESEARCH`, `STRATEGY`, `GITHUB`, `APPLICATION`, `MONITORING`.
- Controlled tool registry and 4-tier autonomy governance model.
- Mandatory Level 3 human approval gating for external consequential actions.
- Interactive Agent Workspace UI at `/agents`.

---

## [0.8.0] - 2026-11-14 - Phase 8: NEXUS Pulse (Monitoring & Notifications)
- Flyway V10 migration: `notifications`, `notification_preferences`, `notification_deliveries`.
- Decoupled domain event publisher and listeners.
- Automated deadline scanner for opportunities, applications, and roadmaps.
- Multi-channel delivery routing (`IN_APP`, `EMAIL`) and quiet-hours policy.
- Real-time Pulse Activity Center at `/activity` and top navigation alert badge.

---

## [0.7.0] - 2026-11-14 - Phase 7: Application Management & Execution Pipeline
- Flyway V9 migration: `applications`, `application_stages`, `resumes`, `interviews`, `documents`.
- Kanban pipeline board at `/applications`.
- AI document generator for tailored cover letters and application answers.
- Interview schedule manager with stage tracking.

---

## [0.6.0] - 2026-11-14 - Phase 6: GitHub Intelligence & Open-Source Assistance
- Flyway V8 migration: `github_connections`, `github_repositories`, `github_issues`, `contribution_workspaces`.
- AES-256-GCM token encryption for personal access tokens.
- Issue suitability algorithm (0-100) and AI contribution plan generator.
- Contribution workspace at `/github`.

---

## [0.5.0] - 2026-11-14 - Phase 5: AI-Powered Roadmaps & Task Execution
- Flyway V7 migration: `roadmaps`, `roadmap_milestones`, `roadmap_tasks`, `learning_resources`.
- Milestone scheduling engine and learning resource catalog.
- Interactive roadmap workspace at `/roadmaps`.

---

## [0.4.0] - 2026-11-14 - Phase 4: Intelligence Engine & Personalized Matching
- Flyway V6 migration: `match_scores`, `skill_gaps`, `opportunity_analyses`.
- 6-Dimensional deterministic matching algorithm.
- AI opportunity intelligence evaluations with local fallback.
- Recommendations feed at `/recommendations`.

---

## [0.3.0] - 2026-11-14 - Phase 3: External Source Integrations & Sync Engine
- Flyway V5 migration: `source_sync_runs`.
- Connectors for Arbeitnow API and Google Summer of Code dataset.
- Automated deduplication, canonical URL hashing, and sync audit logging.

---

## [0.2.0] - 2026-11-14 - Phase 2: Opportunity Management & Discovery Engine
- Flyway V3 & V4 migrations: `opportunities`, `opportunity_sources`, `saved_opportunities`.
- Search, filter, pagination, and developer bookmarking.
- Discovery feed at `/opportunities`.

---

## [0.1.0] - 2026-11-14 - Phase 1: Foundation, Authentication & Onboarding
- Flyway V1 & V2 migrations: `users`, `user_profiles`, `skills`, `user_skills`.
- BCrypt password hashing, JWT HttpOnly cookie session management.
- Onboarding wizard and Core Dashboard at `/dashboard`.
