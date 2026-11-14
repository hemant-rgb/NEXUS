#!/bin/bash
# ==============================================================================
# NEXUS Database Automated Backup Script
# Generates timestamped, compressed PostgreSQL dumps with retention pruning.
# ==============================================================================

set -euo pipefail

BACKUP_DIR="${BACKUP_DIR:-$(pwd)/backups}"
POSTGRES_CONTAINER="${POSTGRES_CONTAINER:-nexus-postgres}"
POSTGRES_DB="${POSTGRES_DB:-nexus_db}"
POSTGRES_USER="${POSTGRES_USER:-nexus_user}"
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-nexus_dev_password_change_in_prod}"
RETENTION_DAYS="${RETENTION_DAYS:-7}"

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/${POSTGRES_DB}_backup_${TIMESTAMP}.sql.gz"

mkdir -p "${BACKUP_DIR}"

echo "=========================================================="
echo "NEXUS PostgreSQL Database Backup"
echo "Database:  ${POSTGRES_DB}"
echo "Container: ${POSTGRES_CONTAINER}"
echo "Target:    ${BACKUP_FILE}"
echo "Timestamp: $(date -u +"%Y-%m-%dT%H:%M:%SZ")"
echo "=========================================================="

if docker ps --format '{{.Names}}' | grep -q "^${POSTGRES_CONTAINER}$"; then
    echo "[INFO] Running pg_dump inside container '${POSTGRES_CONTAINER}'..."
    docker exec -e PGPASSWORD="${POSTGRES_PASSWORD}" "${POSTGRES_CONTAINER}" \
        pg_dump -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" --clean --if-exists --no-owner --no-privileges \
        | gzip > "${BACKUP_FILE}"
else
    echo "[INFO] Container '${POSTGRES_CONTAINER}' not running. Attempting direct pg_dump..."
    PGPASSWORD="${POSTGRES_PASSWORD}" pg_dump -h "${POSTGRES_HOST:-localhost}" -p "${POSTGRES_PORT:-5432}" \
        -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" --clean --if-exists --no-owner --no-privileges \
        | gzip > "${BACKUP_FILE}"
fi

# Verify backup integrity
if [ -s "${BACKUP_FILE}" ]; then
    FILE_SIZE=$(ls -lh "${BACKUP_FILE}" | awk '{print $5}')
    echo "[SUCCESS] Backup created successfully: ${BACKUP_FILE} (${FILE_SIZE})"
else
    echo "[ERROR] Backup file is empty or missing! Aborting." >&2
    exit 1
fi

# Retention pruning
echo "[INFO] Pruning backups older than ${RETENTION_DAYS} days in ${BACKUP_DIR}..."
find "${BACKUP_DIR}" -name "${POSTGRES_DB}_backup_*.sql.gz" -mtime +"${RETENTION_DAYS}" -exec rm -f {} + || true
echo "[INFO] Backup procedure completed cleanly."
