# NEXUS Opportunity Sources & Ingestion Specification

This document details the real-world opportunity source connectors, ingestion architecture, security safeguards, entity deduplication, and synchronization policies implemented in Phase 3 of NEXUS.

---

## 1. Source Registry

NEXUS ingests genuine opportunities from verified public sources, mapping raw provider formats into the canonical 8 NEXUS domains:
- Internships
- Full-time jobs
- Google Summer of Code (GSoC)
- Open-source programs
- Hackathons
- Scholarships
- Fellowships
- Competitions

| Source Key | Source Name | Base / Endpoint URL | Ingestion Protocol | Auth | Supported Categories |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `remotive-tech` | Remotive Software Careers | `https://remotive.com/api/remote-jobs?category=software-dev&limit=25` | REST JSON API | Public / None | Full-time jobs, Internships |
| `arbeitnow-tech` | Arbeitnow Developer Opportunities | `https://www.arbeitnow.com/api/job-board-api` | REST JSON API | Public / None | Full-time jobs, Internships |
| `gsoc-programs` | Google Summer of Code Organizations | `https://raw.githubusercontent.com/nishantwrp/gsoc-organizations/master/api/data/2024.json` | Public JSON Dataset | Public / None | Google Summer of Code, Open-source programs |
| `nexus-local-demo` | NEXUS Verified Local Catalog | Embedded / In-Memory Seed | Synthetic Connector | Internal | All 8 Categories |

---

## 2. Ingestion Connectors

### 2.1 Remotive (`RemotiveSourceConnector`)
- **Source Key:** `remotive-tech`
- **Official Documentation:** https://remotive.com/api/remote-jobs
- **Payload Format:** JSON (`{ "jobs": [ ... ] }`)
- **Key Fields Extracted:**
  - `id` -> `externalId`
  - `title` -> `title`
  - `company_name` -> `organization`
  - `url` -> `applicationUrl`
  - `candidate_required_location` -> `location`
  - `tags` -> `requiredSkills`
  - `description` -> sanitized HTML converted to clean text
- **Rate Limit & Politeness:** Max 1 request per 15 minutes in production; responses are cached; strictly HTTPS.
- **Category Inference:** Titles containing `intern`/`internship` are mapped to *Internships*; others default to *Full-time jobs*.

### 2.2 Arbeitnow (`ArbeitnowSourceConnector`)
- **Source Key:** `arbeitnow-tech`
- **Official Documentation:** https://www.arbeitnow.com/api/job-board-api
- **Payload Format:** JSON (`{ "data": [ ... ] }`)
- **Key Fields Extracted:**
  - `slug` -> `externalId`
  - `title` -> `title`
  - `company_name` -> `organization`
  - `url` -> `applicationUrl`
  - `location` & `remote` -> `location` / `remoteAvailable`
  - `tags` -> `requiredSkills`
  - `description` -> sanitized text
- **Rate Limit & Politeness:** Max 1 request per 10 minutes; pagination supports `page` query parameter.

### 2.3 GSoC Organizations (`GsocOrganizationsConnector`)
- **Source Key:** `gsoc-programs`
- **Data Source:** Verified Google Summer of Code participating mentoring organizations archive dataset.
- **Payload Format:** JSON array of mentoring organizations.
- **Key Fields Extracted:**
  - `name` -> `organization` & `title` (`"${name} — GSoC Mentorship Program"`)
  - `url` -> `applicationUrl`
  - `technologies` -> `requiredSkills` (e.g. Python, Rust, C++, Kubernetes)
  - `topics` -> `preferredSkills` (e.g. Machine Learning, Cloud, Security)
  - `description` -> sanitized description
- **Category:** Strictly *Google Summer of Code* or *Open-source programs*.
- **Deadline Handling:** Spring GSoC application period with rolling student proposal review.

---

## 3. Security & Anti-SSRF Guardrails (`IngestionHttpClient`)

To prevent Server-Side Request Forgery (SSRF) and external denial-of-service risks, all connector network I/O passes exclusively through `IngestionHttpClient`:

1. **Protocol Restriction:** Only `https://` schemes are permitted. Plain HTTP is rejected.
2. **DNS & Private Subnet Blocking:** Before opening any socket, destination hostnames are resolved via DNS. If resolved IP addresses fall within forbidden subnets, the request is immediately aborted with a `SecurityException`:
   - Loopback addresses (`127.0.0.0/8`, `::1`)
   - RFC 1918 Private ranges (`10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`)
   - Link-local and cloud metadata addresses (`169.254.169.254`, `169.254.0.0/16`)
   - Multicast and broadcast ranges
3. **Payload Size Caps:** HTTP response bodies are capped at 5 MB (`5 * 1024 * 1024` bytes) to prevent out-of-memory denial-of-service from massive remote streams.
4. **Timeouts:** Connect timeout: 5,000 ms; Read timeout: 10,000 ms.
5. **Retries:** Configured exponential backoff (up to 2 retries on 5xx or transient connection errors).

---

## 4. Normalization & Deduplication Pipeline

External opportunity data passes through `OpportunityNormalizer` before persistence:

```
[External Source API]
         │
         ▼
[IngestionHttpClient] (SSRF Check, HTTPS only, 5MB Limit)
         │
         ▼
[OpportunityNormalizer]
  ├─ HTML Tag Stripping & Entity Decoding
  ├─ Prompt Injection Neutralization (`[FILTERED]`)
  ├─ URL Canonicalization (strip `utm_*`, `ref`, `fbclid`, lowercase host, strip trailing slash)
  └─ Canonical Category Resolution (8 canonical domains)
         │
         ▼
[DiscoverySyncService Dual-Tier Resolution]
  ├─ Tier 1: Exact Provenance `(source_id, external_id)` in `opportunity_listings`
  │    └─ Match found? Update existing opportunity, refresh `last_seen_at`.
  ├─ Tier 2: Canonical Application URL in `opportunities`
  │    └─ Match found? Link new `opportunity_listing` to canonical record, increment duplicates count.
  ├─ Tier 3: Case-insensitive normalized `(title, organization)`
  │    └─ Match found? Link new `opportunity_listing` to canonical record.
  └─ Tier 4: No match found
       └─ Insert new canonical `Opportunity` and child `OpportunityListing`.
```

---

## 5. Synchronization Engine & Freshness Policy

- **Orchestration:** Managed by `DiscoverySyncService`.
- **Scheduled Interval:** Configurable cron (`nexus.discovery.sync-cron`, default: every 6 hours `0 0 */6 * * *`).
- **Telemetry & History:** Every synchronization execution is recorded in the `sync_runs` table with:
  - `started_at` & `completed_at`
  - `records_fetched`, `records_created`, `records_updated`, `records_skipped`, `duplicates_detected`
  - `status`: `IN_PROGRESS`, `SUCCESS`, or `FAILED`
  - `error_message` & `execution_duration_ms`
- **Failure Isolation:** Connectors run inside isolated `try-catch` blocks. If an external API is down or rate-limited, the run is logged as `FAILED`, setting the source status to `ERROR`, without failing other sources or crashing the server.
- **Conservative Freshness:** NEXUS never deletes opportunities that disappear from an external feed. Opportunities retain `OPEN` status unless their deadline has passed (in which case they transition to `EXPIRED`), preserving user bookmarks and personal preparation notes.
