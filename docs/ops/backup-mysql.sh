#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="/opt/project-issue-hub"
BACKUP_DIR="/data/backups/project-issue-hub/mysql"
DB_NAME="opl_platform"
DB_USER="root"
DB_PASS="root123456"
CONTAINER_NAME="opl-mysql"

mkdir -p "${BACKUP_DIR}"

STAMP="$(date +%F_%H%M%S)"
OUT_FILE="${BACKUP_DIR}/${DB_NAME}_${STAMP}.sql"

echo "[INFO] dump mysql to ${OUT_FILE}"
docker exec "${CONTAINER_NAME}" mysqldump -u"${DB_USER}" -p"${DB_PASS}" "${DB_NAME}" > "${OUT_FILE}"

echo "[INFO] done"
