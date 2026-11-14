#!/bin/bash
# ==============================================================================
# NEXUS Database Restoration Script
# Restores a compressed PostgreSQL dump into target database with validation.
# ==============================================================================

set -euo pipefail

if [ "$#" -lt 1 ]; then
    echo "Usage: $0 <path_to_backup.sql.gz> [target_database_name]"
    exit 1
fi

BACKUP_FILE="$1"
TARGET_DB="${2:-${POSTGRES_DB:-nexus_db}}"
POSTGRES_CONTAINER="${POSTGRES_CONTAINER:-nexus-postgres}"
POSTGRES_USER="${POSTGRES_USER:-nexus_user}"
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-nexus_dev_password_change_in_prod}"

if [ ! -f "${BACKUP_FILE}" ]; then
    echo "[ERROR] Backup file not found: ${BACKUP_FILE}" >&2
    exit 1
fi

echo "=========================================================="
echo "NEXUS PostgreSQL Database Restoration"
echo "Source File: ${BACKUP_FILE}"
echo "Target DB:   ${TARGET_DB}"
echo "Container:   ${POSTGRES_CONTAINER}"
echo "=========================================================="

# Check if container is running
if docker ps --format '{{.Names}}' | grep -q "^${POSTGRES_CONTAINER}$"; then
    echo "[INFO] Ensuring target database '${TARGET_DB}' exists in '${POSTGRES_CONTAINER}'..."
    docker exec -e PGPASSWORD="${POSTGRES_PASSWORD}" "${POSTGRES_CONTAINER}" \
        psql -U "${POSTGRES_USER}" -d postgres -tc "SELECT 1 FROM pg_database WHERE datname = '${TARGET_DB}'" \
        | grep -q 1 || docker exec -e PGPASSWORD="${POSTGRES_PASSWORD}" "${POSTGRES_CONTAINER}" \
        psql -U "${POSTGRES_USER}" -d postgres -c "CREATE DATABASE ${TARGET_DB};"

    echo "[INFO] Restoring database '${TARGET_DB}' from '${BACKUP_FILE}'..."
    gunzip -c "${BACKUP_FILE}" | docker exec -i -e PGPASSWORD="${POSTGRES_PASSWORD}" "${POSTGRES_CONTAINER}" \
        psql -U "${POSTGRES_USER}" -d "${TARGET_DB}" -q

    echo "[INFO] Verifying restored schema in '${TARGET_DB}'..."
    TABLE_COUNT=$(docker exec -e PGPASSWORD="${POSTGRES_PASSWORD}" "${POSTGRES_CONTAINER}" \
        psql -U "${POSTGRES_USER}" -d "${TARGET_DB}" -t -A -c "SELECT count(*) FROM information_schema.tables WHERE table_schema='public';")
    echo "[SUCCESS] Database restoration complete. Public tables in '${TARGET_DB}': ${TABLE_COUNT}"
else
    echo "[ERROR] Container '${POSTGRES_CONTAINER}' is not running! Cannot proceed with restore." >&2
    exit 1
fi
