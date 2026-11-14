# NEXUS — AI-Powered Opportunity Intelligence & Execution Platform

[![CI](https://github.com/nexus-org/nexus/actions/workflows/ci.yml/badge.svg)](https://github.com/nexus-org/nexus/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21_LTS-orange.svg)](https://adoptium.net/)
[![Next.js](https://img.shields.io/badge/Next.js-14_App_Router-black.svg)](https://nextjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)

NEXUS is an open-source opportunity intelligence and execution platform built for university students, researchers, open-source contributors, and software engineers. It bridges the gap between discovering high-impact opportunities (internships, GSoC, open-source programs, hackathons, fellowships) and strategically preparing and executing applications with autonomous AI agents.

---

## 1. Product Workflow

```
DISCOVER → UNDERSTAND → MATCH → PREPARE → EXECUTE → TRACK & MONITOR
```

1. **Discover**: Ingest opportunities from real-world sources (Arbeitnow, GSoC, Remotive) and open-source repositories.
2. **Understand**: Grounded AI analysis extracts technical expectations, key deadlines, and organizational profiles.
3. **Match**: 6-Dimensional deterministic matching engine identifies skill overlaps and eligibility concerns.
4. **Prepare**: NEXUS Strategy synthesizes personalized milestone roadmaps and verified learning paths.
5. **Execute**: Manage applications across a 9-stage Kanban pipeline with tailored cover letters, resumes, and interview trackers.
6. **Track & Orchestrate**: NEXUS Pulse monitors deadlines in real-time, while Autonomous Agents (`SCOUT`, `RESEARCH`, `STRATEGY`, `GITHUB`, `APPLICATION`, `MONITORING`) orchestrate multi-step workflows with Level 3 human approval gating.

---

## 2. Architecture & Tech Stack

NEXUS is engineered as a secure, production-hardened modular monolith:

- **Frontend (`apps/web`)**: Next.js 14 (App Router), TypeScript, Tailwind CSS, Lucide Icons, React Hook Form, Zod. Features a high-density, dark developer aesthetic (#0B0D10 background, #A3E635 accent).
- **Backend (`apps/backend`)**: Java 21 LTS, Spring Boot 3.3.5, Spring Security, Spring Data JPA, Hibernate, Bean Validation.
- **Database (`infrastructure/docker`)**: PostgreSQL 16 managed via Flyway schema migrations (`V1` through `V11`).
- **Session Architecture**: Stateless JWT with secure `HttpOnly`, `SameSite=Lax` cookies, rate limiting, and request correlation IDs.
- **AI Abstraction**: Multi-provider intelligence layer supporting Google Gemini, OpenAI, and zero-dependency local grounded fallback.
- **Safety Governance**: 4-Tier Autonomy model (Level 0 Read-Only, Level 1 Drafting, Level 2 Internal Action, Level 3 Mandatory Human Approval Gating).

---

## 3. Quick Start (Self-Hosting with Docker Compose)

The fastest way to deploy NEXUS locally or on a private server:

```bash
# 1. Clone repository
git clone https://github.com/nexus-org/nexus.git
cd nexus

# 2. Copy environment template
cp .env.example .env

# 3. Launch full platform stack (Postgres + Backend + Frontend)
docker compose up -d

# 4. Access Platform
# Web UI:       http://localhost:3000
# REST API:     http://localhost:8080/api/v1
# Swagger UI:   http://localhost:8080/swagger-ui.html
# Health Check: http://localhost:8080/actuator/health
```

---

## 4. Local Development Setup

For developers contributing to backend or frontend modules:

### Prerequisites:
- Java 21 LTS (Eclipse Temurin recommended)
- Node.js 20 LTS & npm
- Docker (for PostgreSQL)
- Maven 3.9+

### 1. Database
```bash
cp .env.example .env
docker compose up -d nexus-postgres
```

### 2. Backend Service
```bash
cd apps/backend
mvn spring-boot:run
```

### 3. Frontend Console
```bash
cd apps/web
npm install
npm run dev
```

---

## 5. Automated Backups & Disaster Recovery

NEXUS includes automated, compressed database backup and restoration utilities:

```bash
# Create a timestamped, gzip-compressed database backup:
./infrastructure/scripts/backup-db.sh

# Restore a backup into target database:
./infrastructure/scripts/restore-db.sh backups/nexus_db_backup_<timestamp>.sql.gz [target_db]
```

---

## 6. Testing & Quality Gates

Run the automated test suites locally:

```bash
# Backend unit and integration tests (107 tests)
cd apps/backend && mvn test

# Frontend unit and component tests (30 tests)
cd apps/web && npm test

# Next.js standalone production build
cd apps/web && npm run build

# End-to-end user journey tests (9 journeys)
cd apps/web && npx playwright test
```

---

## 7. Documentation Index

- [System Audit Report](docs/SYSTEM_AUDIT_REPORT.md)
- [Production Readiness Report](docs/PRODUCTION_READINESS_REPORT.md)
- [Self-Hosting & Deployment Guide](docs/DEPLOYMENT_GUIDE.md)
- [Testing Verification Report](docs/TESTING_REPORT.md)
- [Open-Source Release Checklist](docs/OPEN_SOURCE_RELEASE_CHECKLIST.md)
- [Autonomous Agents Specification (Phase 9)](docs/PHASE_9_AGENTS_REPORT.md)
- [Pulse Monitoring & Notifications Specification (Phase 8)](docs/PHASE_8_PULSE_REPORT.md)
- [Application Pipeline & Execution Guide](docs/APPLICATIONS.md)
- [GitHub Intelligence & Open-Source Engine](docs/GITHUB.md)
- [Strategy Roadmaps Specification](docs/ROADMAPS.md)
- [Matching Engine Specification](docs/INTELLIGENCE.md)
- [Opportunity Ingestion Specification](docs/SOURCES.md)
- [System Architecture](docs/ARCHITECTURE.md)
- [Database Schema & Migrations](docs/DATABASE.md)
- [REST API Contracts](docs/API.md)
- [Security Guardrails](SECURITY.md)

---

## 8. License

NEXUS is open-source software licensed under the [Apache License 2.0](LICENSE).
