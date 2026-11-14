# NEXUS — Release Notes: Phase 2 (v0.2.0-alpha)

**Release Title:** Opportunity Management & Discovery Engine (NEXUS Scout)  
**Release Date:** November 14, 2026  
**Quality Gate Status:** All Quality Gates Passed (Backend, Frontend, End-to-End, Security Audit)

---

## 1. Release Overview

Phase 2 introduces **NEXUS Scout**, the opportunity intelligence and discovery foundation of NEXUS. Built as an extension to the Phase 1 modular monolith, Phase 2 provides full lifecycle discovery, filtering, and bookmarking of high-impact technical opportunities for developers, researchers, and students.

### Supported Opportunity Categories (8 Canonical Domains)
1. **Internships** (Summer and off-cycle engineering internships)
2. **Full-time jobs** (Early-career software engineering roles)
3. **Google Summer of Code (GSoC)** (Open-source mentorship programs)
4. **Open-source programs** (LFX, MLH Fellowship, outreach programs)
5. **Hackathons** (Virtual and global technical competitions)
6. **Scholarships** (Tech conference and academic fellowships)
7. **Fellowships** (Entrepreneurial and research grants)
8. **Competitions** (Algorithmic and systems coding arenas)

---

## 2. Key Architecture & Database Changes

### Relational Schema Enhancements (Flyway Migrations V3 & V4)
- **`opportunities`**: Canonical opportunity specifications. Uses `UUID` primary keys, `DATE` for deadline/start/end (avoiding timezone offset distortion), PostgreSQL `TEXT[]` arrays for skills matrix requirements, and composite B-tree indexes on `(status, deadline)` and `(category, status)`.
- **`opportunity_sources`**: Ingestion registry tracking source key, connector type, base URL, and telemetry timestamps.
- **`opportunity_listings`**: Provenance junction mapping external IDs to canonical opportunities. Unique constraint on `(source_id, external_id)` guarantees duplicate external listings cannot collide.
- **`saved_opportunities`**: User bookmarking junction with developer preparation notes and a unique constraint on `(user_id, opportunity_id)`.

### Provider-Independent Source Connector SPI
- `OpportunitySourceConnector`: Decoupled connector contract allowing asynchronous and multi-protocol data ingestion.
- Built-in connectors:
  - `LocalDemoSourceConnector` (`nexus-local-demo`): Curated seed dataset across all 8 opportunity categories.
  - `VerifiedFeedSourceConnector` (`os-programs-curated`): External HTTPS feed consumer with SSRF defense (blocks non-HTTPS URLs and private IPv4/IPv6 ranges `10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`, `127.0.0.0/8`) and HTML/prompt sanitization before database insertion.

### Cross-Source Deduplication & Ingestion Pipeline
- `DiscoverySyncService`: Dual-tier deduplication checking provenance listings by `(sourceId, externalId)` first, followed by canonical similarity matching on `(title, organization)`.
- Automated periodic ingestion via Spring `@Scheduled` (`0 0 */6 * * *` default).
- Administrative manual sync endpoint protected with `@PreAuthorize("hasRole('ADMIN')")`.

---

## 3. REST API Contracts (`/api/v1`)

| Method | Endpoint | Access | Purpose |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/opportunities` | Public | Paginated opportunity search, category & remote filtering, sorting |
| `GET` | `/api/v1/opportunities/{id}` | Public | Opportunity specification by ID (includes `isSaved` if logged in) |
| `GET` | `/api/v1/opportunities/categories` | Public | Opportunity category distribution counts |
| `GET` | `/api/v1/opportunities/featured` | Public | Highlighted opportunity showcase (up to 6 items) |
| `GET` | `/api/v1/users/me/saved-opportunities` | User Auth | Fetch all opportunities saved by authenticated developer |
| `POST` | `/api/v1/users/me/saved-opportunities` | User Auth | Bookmark opportunity with optional developer notes |
| `PATCH` | `/api/v1/users/me/saved-opportunities/{id}` | User Auth | Update personal developer notes |
| `DELETE` | `/api/v1/users/me/saved-opportunities/{id}` | User Auth | Remove bookmark |
| `GET` | `/api/v1/sources` | Public | List registered external ingest sources |
| `POST` | `/api/v1/sources/{key}/sync` | Admin Auth | Manually trigger sync pipeline for a specific connector |

---

## 4. Frontend Command Center Experience

- **Discovery Matrix (`/opportunities`)**: High information density technical grid, category filter pills, keyword search, remote toggle, and pagination controls.
- **Opportunity Detail Specification (`/opportunities/[id]`)**: Full technical specifications, required vs. preferred skill badges, deadline telemetry, official application portals, and personal developer preparation notes.
- **Saved Bookmarks Management (`/saved`)**: Overview of shortlisted targets, deadline indicators, inline note editing, and one-click removal.
- **Design System**: Strict adherence to the dark developer aesthetic (`#0B0D10` main background, `#151A21` card surfaces, `#29313B` borders, `#A3E635` electric green accents).

---

## 5. Verification & Quality Assurance Summary

### Backend Test Suite (Maven / JUnit 5 / Spring Boot Test)
- **Tests Executed:** 29
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0
- **Coverage Areas:** Opportunity searching & filtering, bookmarking & duplicate rejection, personal notes modification, multi-user security isolation, cross-source deduplication, SSRF defense, prompt sanitization, and REST API contracts.

### Frontend Unit & Component Tests (Vitest / React Testing Library)
- **Test Files:** 7
- **Tests Executed:** 15
- **Failures:** 0
- **Coverage Areas:** Opportunity cards, discovery feed filtering, saved opportunity notes inline editing, opportunity details rendering, error states, and auth forms.

### End-to-End User Journeys (Playwright / Chromium)
- **Journeys Executed:** 3
- **Failures:** 0
- **Journey A (New User Journey):** Registration -> Onboarding -> Skills -> Dashboard -> Persistence -> Logout. (Passed)
- **Journey B (Discover & Save Journey):** Discover -> Search -> Category Filter -> Inspect Details -> Save -> Annotate Notes -> Saved Feed -> Verify Persistence -> Remove. (Passed)
- **Journey C (Security Isolation Journey):** Multi-user isolation verification; confirms User B cannot view, modify, or delete User A's bookmarks or notes. (Passed)

### Static Analysis & Type Checking
- **ESLint:** 0 warnings, 0 errors (`next lint`).
- **TypeScript:** 0 type errors (`tsc --noEmit`).
- **Next.js Production Build:** Successfully compiled all 14 routes (`npm run build`).

---

## 6. Known Limitations & Planned for Phase 3

1. **AI Match Scoring**: Opportunity compatibility scoring based on developer skills matrix is planned for Phase 3 (NEXUS Strategy).
2. **Automated Submission**: Direct application dispatching and GitHub contribution tracking are scheduled for Phase 3/4.
3. **Connector Rate Limiting**: Ingestion pipelines currently rely on standard HTTP backoff; distributed rate limiting will be introduced as additional external connectors are onboarded.
