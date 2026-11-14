# NEXUS System Architecture — Phase 2: Opportunity Management & Discovery Engine

## 1. Architecture Overview

NEXUS is engineered as a high-performance **modular monolith** with clear domain boundaries, strict separation of concerns, and an API-first contract between the Next.js frontend and Spring Boot backend.

```
+-------------------------------------------------------------------------+
|                       NEXUS Client (Next.js 14)                         |
|  - App Router / React Server & Client Components                        |
|  - Command Center Shell (Sidebar, TopNav, Breadcrumbs)                  |
|  - Discovery Engine UI (/opportunities, /opportunities/[id])            |
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
|  | - Saved Opportunities & Annotation Notes      |   | - Provenance  |  |
|  +-----------------------------------------------+   +---------------+  |
|                                                                         |
|  +-----------------------------------------------+                      |
|  |            Discovery Sync Module              |                      |
|  | - Scheduled Sync Engine (@Scheduled cron)    |                      |
|  | - Deduplication & Normalization               |                      |
|  | - Content Sanitization & SSRF Protection      |                      |
|  +-----------------------------------------------+                      |
+-------------------------------------------------------------------------+
                                     |
                              Spring Data JPA
                                     |
+-------------------------------------------------------------------------+
|                    Data Persistence (PostgreSQL 16)                     |
|  - Flyway Migrations (V1, V2, V3, V4)                                    |
|  - UUID Primary Keys & UTC Timestamps                                   |
|  - Date-only Deadlines & Strict Unique Constraints                      |
+-------------------------------------------------------------------------+
```

## 2. Core Modules & Phase 2 Capabilities

### 2.1 Opportunity Discovery (`com.nexus.opportunity`)
- **Specification-driven Filtering**: Dynamic JPA Predicates supporting full-text keyword search across titles, organizations, and descriptions; category selection; remote-only filtering; location and organization constraints; and deadline bounding.
- **Date-only Deadlines**: Deadlines are modeled with `LocalDate` without timezone shift corruption, accompanied by `deadline_type` (`STRICT`, `ROLLING`, `ESTIMATED`, `UNKNOWN`).
- **Saved Opportunities & Annotations**: Users can bookmark opportunities and attach personal strategic preparation notes. Data isolation is strictly enforced via the authenticated user's `SecurityContext`.

### 2.2 Source Connector Architecture (`com.nexus.source.connector`)
- **Provider-Independent Connectors**: Implement `OpportunitySourceConnector` with standard contracts:
  - `String getSourceKey()`
  - `SourceMetadata getMetadata()`
  - `SyncResult fetchOpportunities(SyncRequest request)`
- **Normalized Opportunity Model**: Incoming records from any source are mapped to `NormalizedOpportunity`, decoupling upstream schemas from the NEXUS domain model.
- **Implemented Connectors**:
  - `LocalDemoSourceConnector` (`nexus-local-demo`): Offline verified catalog with opportunities across all 8 supported categories.
  - `VerifiedFeedSourceConnector` (`os-programs-curated`): Live public open-source programs connector with SSRF URL inspection, timeout guardrails, and prompt-injection sanitization.

### 2.3 Discovery Synchronization (`com.nexus.discovery`)
- **Deduplication Strategy**:
  1. Primary check: `OpportunityListing` matching `(source_id, external_id)`.
  2. Secondary check: Case-insensitive match on `(title, organization)`.
  3. Existing records are updated with updated fields and `lastSeenAt` refreshed without duplicate creation.
- **Automated Scheduling**: Scheduled synchronization runs periodically via Spring Scheduling (`@Scheduled`), with manual admin sync triggers available via protected endpoints.
