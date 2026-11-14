# NEXUS Local Development Guide

## 1. Prerequisites
- **Node.js**: v20+ (Node v26 verified)
- **Java**: OpenJDK 21 LTS+ (OpenJDK 26 verified)
- **Maven**: 3.9+
- **Docker & Docker Compose**: Docker 26+

## 2. Environment Setup

Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```

Start PostgreSQL container via Docker Compose:
```bash
docker compose up -d
```
Verify container status:
```bash
docker ps
# Expected: nexus-postgres (healthy) on port 5432
```

## 3. Running Backend (Spring Boot)

Navigate to `apps/backend`:
```bash
cd apps/backend
mvn spring-boot:run
```
- Server starts on `http://localhost:8080`
- Actuator Health check: `http://localhost:8080/actuator/health`
- OpenAPI Specification: `http://localhost:8080/api/v1/api-docs`
- Swagger UI Documentation: `http://localhost:8080/swagger-ui.html`

Run backend tests:
```bash
mvn test
```

## 4. Running Frontend (Next.js)

Navigate to `apps/web`:
```bash
cd apps/web
npm install
npm run dev
```
- Web console available at `http://localhost:3000`
- Production build: `npm run build`
- Run frontend test suite: `npm test`
- Run E2E test suite: `npx playwright test`

## 5. Opportunity Ingestion & Background Sync

The backend contains a provider-independent external source sync engine:
- Connectors register dynamically via Spring's `List<OpportunitySourceConnector>`.
- Built-in connectors:
  - `nexus-local-demo`: Local curated opportunities across all 8 supported categories.
  - `os-programs-curated`: External feed connector with SSRF defense (HTTPS only, RFC 1918 private IP rejection) and prompt sanitization.
- **Scheduled Sync**: Automatically runs periodically (`opportunity.sync.cron="0 0 */6 * * *"` by default, configurable in `application.yml`).
- **Manual Trigger (Admin)**:
  ```bash
  curl -X POST http://localhost:8080/api/v1/sources/nexus-local-demo/sync \
    -H "Cookie: nexus_session=<ADMIN_JWT>"
  ```

## 6. Troubleshooting
- **Database Connection Refused**: Verify PostgreSQL container is running (`docker compose ps`). Check that port 5432 is not occupied by another local service.
- **CORS/Session Cookie Issue**: Ensure the frontend calls `/api/v1` with `credentials: "include"`. In development, Next.js rewrites forward requests to `http://localhost:8080`.
- **Flyway Migration Failure**: If schema changes are modified after migration, execute `docker compose down -v` to reset the test volume.
