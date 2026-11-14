# NEXUS System Architecture — Phase 1

## 1. Architecture Overview

NEXUS is designed as a high-performance **modular monolith** with a strict separation of concerns between the presentation layer and backend domain services.

```
+------------------------------------------------------------------+
|                    NEXUS Client (Next.js 14)                     |
|  - App Router / React Server & Client Components                 |
|  - Command Center Shell (Sidebar, TopNav, Onboarding Wizard)     |
|  - Dark Developer Aesthetic / High Information Density           |
+------------------------------------------------------------------+
                                |
                     HTTP / REST (HttpOnly Cookie)
                                |
+------------------------------------------------------------------+
|               NEXUS Backend Core (Spring Boot 3.3.5)              |
|                                                                  |
|  +---------------------+   +---------------------+               |
|  |     Auth Module     |   |   Profile Module    |               |
|  | - JWT Session Mgr   |   | - Developer Profile |               |
|  | - BCrypt Hashing    |   | - Opportunity Prefs |               |
|  +---------------------+   +---------------------+               |
|                                                                  |
|  +---------------------+   +---------------------+               |
|  |    Skills Module    |   |   Common & Security |               |
|  | - Skills Catalog    |   | - Error Handling    |               |
|  | - Proficiency Matrix|   | - OpenAPI / Actuator|               |
|  +---------------------+   +---------------------+               |
+------------------------------------------------------------------+
                                |
                         Spring Data JPA
                                |
+------------------------------------------------------------------+
|               Data Persistence (PostgreSQL 16)                   |
|  - Flyway Versioned Migrations                                   |
|  - UUID Primary Keys & UTC Timestamps                            |
+------------------------------------------------------------------+
```

## 2. Core Modules

### 2.1 Authentication & Security (`com.nexus.auth` & `com.nexus.common.security`)
- **Stateless Session with Secure Cookies**: Issues signed HMAC-SHA256 JWT tokens transmitted strictly via `HttpOnly`, `SameSite=Lax` cookies named `nexus_session`.
- **Zero LocalStorage Tokens**: Eliminates vulnerability to client-side XSS token theft.
- **BCrypt Adaptive Hashing**: Salted and computationally adaptive password hashing.
- **Centralized Authentication Filter**: `JwtAuthenticationFilter` intercepts requests, validates signatures, and injects `UserPrincipal` into Spring's `SecurityContext`.

### 2.2 Developer Profile & Identity (`com.nexus.profile` & `com.nexus.user`)
- Tracks academic institution, graduation year, degree, bio, and GitHub handle.
- Stores multi-step onboarding progression state (`onboarding_step` and `onboarding_completed`).
- Enforces strict data isolation: all endpoints resolve user identity through authenticated session principal, preventing IDOR vulnerabilities.

### 2.3 Opportunity Preferences (`com.nexus.profile`)
- Stores delivery mode preferences (`REMOTE`, `ONSITE`, `HYBRID`, `ANY`).
- Captures weekly bandwidth hours and subscribed opportunity categories (GSoC, Internships, Open Source Programs, Hackathons, Fellowships, Scholarships, Competitions).

### 2.4 Technical Skills Matrix (`com.nexus.skills`)
- Pre-seeded catalog of 35+ core programming languages, frameworks, cloud tools, and CS fundamentals.
- Dynamic custom skill registration with automatic normalization and deduplication.
- Experience calibration (proficiency: `Beginner`, `Intermediate`, `Advanced`, and years of experience).

## 3. Technology Stack Justification
- **Java 21 LTS + Spring Boot 3**: Enterprise reliability, strong type safety, mature ecosystem, and rapid runtime performance.
- **PostgreSQL 16**: ACID guarantees, relational integrity with foreign keys, and support for JSONB operations.
- **Next.js 14 App Router + Tailwind CSS**: Zero runtime CSS overhead, optimized static generation, and component modularity.
