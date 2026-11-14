# Contributing to NEXUS

Thank you for your interest in contributing to NEXUS, the open-source Opportunity Intelligence & Execution Platform.

## 1. Development Principles

- **Security First**: All user data must maintain strict tenant isolation. Never bypass authorization checks or expose secrets.
- **Zero Lombok in Java**: NEXUS uses standard Java 21 LTS records, explicit constructors, and accessors.
- **API Contracts**: All REST endpoints under `/api/v1` must return payloads wrapped in `ApiResponse<T>`.
- **Grounded AI**: Deterministic business rules govern eligibility and matching. AI provides explanations and analysis; it never overrides deterministic business policy.
- **Safety Levels for Autonomous Actions**: Autonomous actions that alter external systems or submit applications require explicit Level 3 user authorization.

---

## 2. Setting Up Your Local Environment

### Prerequisites:
- Java 21 LTS (Eclipse Temurin recommended)
- Node.js 20 LTS & npm
- Docker & Docker Compose
- Maven 3.9+

### Quickstart:
1. Clone the repository:
   ```bash
   git clone https://github.com/nexus-org/nexus.git
   cd nexus
   ```
2. Configure local environment:
   ```bash
   cp .env.example .env
   ```
3. Start the PostgreSQL database:
   ```bash
   docker compose up -d nexus-postgres
   ```
4. Start the backend service:
   ```bash
   cd apps/backend
   mvn spring-boot:run
   ```
5. Start the frontend development server:
   ```bash
   cd apps/web
   npm install
   npm run dev
   ```
6. Access the web console at `http://localhost:3000`.

---

## 3. Pull Request Guidelines

1. Create a feature branch from `main`:
   ```bash
   git checkout -b feat/your-feature-name
   ```
2. Run test suites locally before submitting:
   ```bash
   # Backend tests
   cd apps/backend && mvn test

   # Frontend unit tests
   cd apps/web && npm test

   # Frontend build
   npm run build
   ```
3. Ensure commit messages follow Conventional Commits (`feat:`, `fix:`, `docs:`, `test:`).
4. Submit PR against `main` and link any relevant GitHub issues.
