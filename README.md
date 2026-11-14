# NEXUS — AI-Powered Opportunity Intelligence & Execution Platform

[![CI](https://github.com/nexus/nexus/actions/workflows/ci.yml/badge.svg)](https://github.com/nexus/nexus/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

NEXUS is an AI-powered intelligence and execution platform built for university students, computer science researchers, open-source contributors, and software engineers. It bridges the gap between discovering high-impact opportunities (internships, GSoC, open-source programs, hackathons, fellowships) and strategically preparing and executing applications.

---

## Architecture & Core Modules

NEXUS is engineered as a modular monolith:

- **Frontend (`apps/web`)**: Next.js 14 (App Router), TypeScript, Tailwind CSS, Lucide React, React Hook Form, and Zod. Features a distinctive, dark developer aesthetic with high information density.
- **Backend (`apps/backend`)**: Java 21 LTS, Spring Boot 3.3.5, Spring Security, Spring Data JPA, and Bean Validation.
- **Database (`infrastructure/docker`)**: PostgreSQL 16 managed with Flyway migrations.
- **Session Architecture**: Stateless JWT with secure `HttpOnly`, `SameSite=Lax` cookies.

### Product Workflow
```
DISCOVER → UNDERSTAND → MATCH → PREPARE → EXECUTE → TRACK
```

### Module Status
- **Phase 1 (Completed)**: Engineering Foundation, Authentication, Developer Onboarding Wizard, Technical Skills Matrix, and Command Center Shell.
- **Phase 2 (Completed)**: Opportunity Management & Discovery Engine (NEXUS Scout). 8 opportunity categories, provider-independent connector architecture, deduplication, search & filtering, saved opportunities with developer notes, and high-density technical UI.
- **Phase 3 (Completed)**: Real-World Opportunity Source Integrations & Automated Data Ingestion. Active external connectors (Remotive, Arbeitnow, GSoC dataset), SSRF-guarded HTTP client, dual-tier entity resolution, sync telemetry runs, and data provenance UI badges.
- **Phase 4 (Completed)**: NEXUS Intelligence & Personalized Opportunity Matching. 6-dimension deterministic scoring, grounded eligibility state machine with disqualifier score capping, technical skill-gap matrix, provider-independent AI intelligence synthesizer (`LocalFallbackAiProvider`), token usage tracking, and personalized recommendations feed.
- **Phase 5 (Completed)**: NEXUS Strategy (Personalized Preparation Roadmaps & Task Management). Opportunity-based and goal-based roadmap synthesis, ordered milestones, concrete completion criteria, deterministic 5-day study capacity scheduling, dynamic progress telemetry, partial regeneration preserving completed work, and verified learning resource catalog.
- **Phase 6 (Upcoming)**: NEXUS Execution (GitHub Contribution Assistant & Application Workflows).
- **Phase 7 (Upcoming)**: NEXUS Pulse (Unified Progress & Deadline Telemetry Monitor).

---

## Quick Start (Local Setup)

### 1. Start Database Container
```bash
cp .env.example .env
docker compose up -d
```

### 2. Start Backend Service
```bash
cd apps/backend
mvn spring-boot:run
```
- API Base: `http://localhost:8080/api/v1`
- Swagger UI Docs: `http://localhost:8080/swagger-ui.html`
- Actuator Health: `http://localhost:8080/actuator/health`

### 3. Start Frontend Console
```bash
cd apps/web
npm install
npm run dev
```
Open `http://localhost:3000` in your browser.

---

## Testing & Quality Assurance

- **Backend Unit & Integration Tests**:
  ```bash
  cd apps/backend && mvn test
  ```
- **Frontend Unit & Component Tests**:
  ```bash
  cd apps/web && npm test
  ```
- **End-to-End Test Suite**:
  ```bash
  cd apps/web && npx playwright test
  ```

---

## Documentation Links
- [Strategy & Preparation Roadmaps Specification](docs/ROADMAPS.md)
- [Intelligence & Matching Engine Specification](docs/INTELLIGENCE.md)
- [Opportunity Sources & Ingestion Specification](docs/SOURCES.md)
- [System Architecture](docs/ARCHITECTURE.md)
- [Database Schema & Migrations](docs/DATABASE.md)
- [REST API Contracts](docs/API.md)
- [Local Development Guide](docs/DEVELOPMENT.md)
- [Security Guardrails](SECURITY.md)
