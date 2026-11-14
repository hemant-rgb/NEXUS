# NEXUS — Self-Hosting & Deployment Guide

This guide covers deploying NEXUS across three environments:
1. **Local Development**
2. **Self-Hosting (Docker Compose)**
3. **Production Cloud Deployment**

---

## Environment 1: Local Development

### Prerequisites:
- Java 21 LTS (Temurin)
- Node.js 20 LTS & npm
- Docker (for PostgreSQL)
- Maven 3.9+

### Steps:
```bash
# 1. Setup environment file
cp .env.example .env

# 2. Start PostgreSQL container
docker compose up -d nexus-postgres

# 3. Start Spring Boot Backend
cd apps/backend
mvn spring-boot:run

# 4. Start Next.js Frontend (in a separate terminal)
cd apps/web
npm install
npm run dev
```
Access the web application at `http://localhost:3000`.

---

## Environment 2: Self-Hosting (Docker Compose)

For running the full stack on a VPS, homelab, or private server:

### Steps:
```bash
# 1. Clone the repository
git clone https://github.com/nexus-org/nexus.git
cd nexus

# 2. Configure environment
cp .env.example .env
# Edit .env with your desired JWT_SECRET, AI credentials, etc.

# 3. Start the entire platform
docker compose up -d

# 4. Verify running containers and health
docker compose ps
```

### URLs:
- **Web Interface**: `http://<your-server-ip>:3000`
- **REST API**: `http://<your-server-ip>:8080/api/v1`
- **Health Check**: `http://<your-server-ip>:8080/actuator/health`

---

## Environment 3: Production Cloud Deployment

For production deployments behind a reverse proxy (e.g. Caddy, Nginx, Cloudflare) with HTTPS:

### Recommended Architecture:
- **Frontend**: Next.js deployed on Vercel or as a container.
- **Backend**: Docker container on AWS ECS, GCP Cloud Run, or a dedicated Linux VPS.
- **Database**: Managed PostgreSQL (AWS RDS, Supabase, Neon).
- **SSL / Reverse Proxy**: Caddy or Nginx with automatic Let's Encrypt certificates.

### Production Environment Variables (`.env`):
```ini
POSTGRES_DB=nexus_db
POSTGRES_USER=nexus_user
POSTGRES_PASSWORD=<strong_random_password>
SPRING_DATASOURCE_URL=jdbc:postgresql://<db_host>:5432/nexus_db

SERVER_PORT=8080
JWT_SECRET=<64_character_hex_secret>
JWT_EXPIRATION_MS=86400000
JWT_COOKIE_NAME=nexus_session
COOKIE_SECURE=true
COOKIE_SAMESITE=Lax
CORS_ALLOWED_ORIGINS=https://nexus.yourdomain.com

PORT=3000
NEXT_PUBLIC_API_URL=https://api.yourdomain.com/api/v1
BACKEND_INTERNAL_URL=http://nexus-backend:8080

AI_PROVIDER=gemini
AI_MODEL=gemini-1.5-flash
AI_API_KEY=<your_gemini_api_key>
RATE_LIMIT_ENABLED=true
ALLOW_PRIVATE_IPS=false
```

### Running with Production Compose:
```bash
docker compose -f docker-compose.prod.yml up -d
```

---

## Database Migrations

Database schema migrations are executed automatically by Flyway on backend startup. To run migrations manually via Maven:
```bash
cd apps/backend
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/nexus_db -Dflyway.user=nexus_user -Dflyway.password=nexus_dev_password_change_in_prod
```

---

## Backup & Recovery

### Creating a Backup:
```bash
./infrastructure/scripts/backup-db.sh
```
This generates a timestamped file: `backups/nexus_db_backup_<timestamp>.sql.gz`.

### Restoring a Backup:
```bash
./infrastructure/scripts/restore-db.sh backups/nexus_db_backup_<timestamp>.sql.gz [target_db_name]
```

---

## Updating an Existing Deployment

```bash
# 1. Pull latest code
git pull origin main

# 2. Rebuild and restart containers
docker compose down
docker compose build --no-cache
docker compose up -d

# 3. Verify health
docker compose ps
```
