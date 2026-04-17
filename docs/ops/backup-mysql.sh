#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="${PROJECT_DIR:-$(cd "${SCRIPT_DIR}/../.." && pwd)}"
ENV_FILE="${ENV_FILE:-${PROJECT_DIR}/.env}"
BACKUP_BASE_DIR="${BACKUP_BASE_DIR:-/data/backups/project-issue-hub}"
BACKUP_DIR="${BACKUP_DIR:-${BACKUP_BASE_DIR}/mysql}"
CONTAINER_NAME="${MYSQL_CONTAINER_NAME:-opl-mysql}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"

if [[ -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
fi

DB_NAME="${MYSQL_DATABASE:-${MYSQL_DB:-opl_platform}}"
DB_USER="${MYSQL_BACKUP_USER:-${MYSQL_USERNAME:-root}}"
DB_PASS="${MYSQL_BACKUP_PASSWORD:-${MYSQL_PASSWORD:-${MYSQL_ROOT_PASSWORD:-}}}"

if [[ -z "${DB_PASS}" ]]; then
  echo "[ERROR] MYSQL password is empty. Set MYSQL_BACKUP_PASSWORD, MYSQL_PASSWORD, or MYSQL_ROOT_PASSWORD."
  exit 1
fi

mkdir -p "${BACKUP_DIR}"

STAMP="$(date +%F_%H%M%S)"
OUT_FILE="${BACKUP_DIR}/${DB_NAME}_${STAMP}.sql"

echo "[INFO] dump mysql to ${OUT_FILE}"
docker exec "${CONTAINER_NAME}" mysqldump -u"${DB_USER}" -p"${DB_PASS}" "${DB_NAME}" > "${OUT_FILE}"

find "${BACKUP_DIR}" -type f -name "${DB_NAME}_*.sql" -mtime "+${RETENTION_DAYS}" -delete

echo "[INFO] done"
