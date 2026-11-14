# NEXUS System Architecture — Phase 4: Intelligence Engine & Personalized Matching

## 1. Architecture Overview

NEXUS is engineered as a high-performance **modular monolith** with clear domain boundaries, strict separation of concerns, and an API-first contract between the Next.js frontend and Spring Boot backend.

```
+-------------------------------------------------------------------------+
|                       NEXUS Client (Next.js 14)                         |
|  - App Router / React Server & Client Components                        |
|  - Command Center Shell (Sidebar, TopNav, Breadcrumbs)                  |
|  - Recommendations Feed (/recommendations) with Dimension Dials         |
|  - Discovery Engine UI (/opportunities, /opportunities/[id])            |
|  - Match Intelligence & Technical Skill-Gap Matrix Component            |
|  - AI Grounded Intelligence Synthesizer Component                       |
|  - Bookmarks & Annotation Engine (/saved)                               |
|  - Dark Developer Aesthetic / High Information Density                  |
+-------------------------------------------------------------------------+
                                     |
                       HTTP / REST (HttpOnly Cookie)
                                     |
+-------------------------------------------------------------------------+
|                  NEXUS Backend Core (Spring Boot 3.3.5)                 |
|                                                                         |
|  +---------------------+   +---------------------+   +---------------+  |
|  |     Auth Module     |   |   Profile Module    |   | Skills Module |  |
|  | - JWT Session Mgr   |   | - Developer Profile |   | - Catalog     |  |
|  | - BCrypt Hashing    |   | - Opportunity Prefs |   | - Matrix      |  |
|  +---------------------+   +---------------------+   +---------------+  |
|                                                                         |
|  +-----------------------------------------------+   +---------------+  |
|  |            Opportunity Module                 |   | Source Module |  |
|  | - Dynamic Search & Specification Filtering    |   | - Connector   |  |
|  | - Category Aggregations & Status Lifecycle    |   |   Framework   |  |
|  | - Saved Opportunities & Annotation Notes      |   | - Ingestion   |  |
|  | - Canonical URL & Provenance Tracking         |   |   HttpClient  |  |
|  +-----------------------------------------------+   +---------------+  |
|                                                                         |
|  +-------------------------------------------------------------------+  |
|  |                    Discovery Sync Module                          |  |
|  | - Automated Sync Engine (@Scheduled 6-hour cron)                  |  |
|  | - Dual-Tier Resolution (Provenance -> Canonical URL -> Name)     |  |
|  | - Content Normalization & Prompt-Injection Neutralization         |  |
|  | - SyncRun Telemetry & Metrics Tracking (Created/Updated/Dups)     |  |
|  +-------------------------------------------------------------------+  |
|                                                                         |
|  +-----------------------------------+   +---------------------------+  |
|  |          Matching Module          |   |    Intelligence Module    |  |
|  | - 6-Dimension Deterministic Engine|   | - Provider Abstraction    |  |
|  | - Grounded Eligibility Assessor   |   | - Local Fallback Provider |  |
|  | - Weighted Skill Gap Matrix       |   | - Token & Cost Telemetry  |  |
|  | - Ineligibility Score Capping     |   | - Prompt Injection Guard  |  |
|  +-----------------------------------+   +---------------------------+  |
+-------------------------------------------------------------------------+
                                     |
                               Spring Data JPA
                                     |
+-------------------------------------------------------------------------+
|                    Data Persistence (PostgreSQL 16)                     |
|  - Flyway Migrations (V1 through V6)                                    |
|  - Matching: opportunity_matches (Dimensions & Factors)                 |
|  - Intelligence: opportunity_analyses & ai_usage_records                |
|  - Ingestion Telemetry: sync_runs Table & Counters                      |
|  - UUID Primary Keys & UTC Timestamps                                   |
+-------------------------------------------------------------------------+
```

## 2. Core Modules & Phase 4 Capabilities

### 2.1 Personalized Matching Engine (`com.nexus.matching`)
- **6-Dimension Deterministic Model**: Computes weighted match score $[0.0, 100.0]$ across Skills (30%), Eligibility (25%), Interest (15%), Experience (15%), Deadline (10%), and Location (5%).
- **Hard Eligibility Boundary**: Enforces strict score capping at $\le 35.0\%$ when mandatory criteria are violated (`INELIGIBLE`).
- **Skill Gap Normalization**: Resolves aliases (e.g. `ReactJS` $\rightarrow$ `react`) and computes priority-ranked gap matrices (`MATCHED`, `PARTIALLY_MATCHED`, `MISSING`).

### 2.2 Grounded AI Intelligence (`com.nexus.intelligence`)
- **Decoupled AI Architecture**: Implements `AiProvider` interface with automatic offline fallback (`LocalFallbackAiProvider`).
- **Security & Telemetry**: Encapsulates external scraping data within `<untrusted_content>` tags, tracks prompt versions (`v1.0`), and audits token usage in `ai_usage_records`.

## 2. Core Modules & Phase 3 Capabilities

### 2.1 Opportunity Discovery (`com.nexus.opportunity`)
- **Specification-driven Filtering**: Dynamic JPA Predicates supporting full-text keyword search across titles, organizations, and descriptions; category selection; remote-only filtering; location and organization constraints; and deadline bounding.
- **Date-only Deadlines**: Deadlines are modeled with `LocalDate` without timezone shift corruption, accompanied by `deadline_type` (`STRICT`, `ROLLING`, `ESTIMATED`, `UNKNOWN`).
- **Saved Opportunities & Annotations**: Users can bookmark opportunities and attach personal strategic preparation notes. Data isolation is strictly enforced via the authenticated user's `SecurityContext`.
- **Data Provenance & Verification Tracking**: Each opportunity card and detail page displays the verified data source and timestamp of last verification.

### 2.2 Source Connector Architecture (`com.nexus.source.connector`)
- **Provider-Independent Connectors**: Implement `OpportunitySourceConnector` with standard contracts:
  - `String getSourceKey()`
  - `SourceMetadata getMetadata()`
  - `SyncResult fetchOpportunities(SyncRequest request)`
- **SSRF-Guarded HTTP Client (`IngestionHttpClient`)**: Enforces HTTPS, validates IP addresses against private and cloud metadata CIDRs before socket connection, imposes 5MB response size caps, and provides exponential backoff retries.
- **Implemented Real Connectors**:
  - `RemotiveSourceConnector` (`remotive-tech`): Ingests global remote software engineering jobs and internships.
  - `ArbeitnowSourceConnector` (`arbeitnow-tech`): Ingests European and remote engineering roles.
  - `GsocOrganizationsConnector` (`gsoc-programs`): Ingests verified Google Summer of Code mentoring organizations, extracting project archives and technical stacks.
  - `LocalDemoSourceConnector` (`nexus-local-demo`): Offline verified reference catalog across all 8 supported categories.

### 2.3 Discovery Synchronization & Deduplication (`com.nexus.discovery`)
- **Dual-Tier Resolution Strategy**:
  1. **Tier 1 (Exact Provenance)**: Match `OpportunityListing` by `(source_id, external_id)`. If present, update fields and refresh `last_seen_at`.
  2. **Tier 2 (Cross-Source Canonical URL)**: Match canonicalized application URL (stripped of `utm_*`, `ref`, and tracking tokens). If present, link the new source listing to the existing opportunity without creating duplicate cards.
  3. **Tier 3 (Normalized Identity)**: Match case-insensitive `(title, organization)`. If present, link listing to the existing record.
  4. **Tier 4 (New Record)**: Persist new canonical `Opportunity` and its initial `OpportunityListing`.
- **Sync Telemetry (`sync_runs`)**: Every synchronization execution records fetched, created, updated, skipped, and duplicate metrics with timing duration.
- **Failure Isolation**: Connector errors are isolated per source; failures are logged with error messages and source status set to `ERROR`, without failing other sources or crashing the server.
