#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="${PROJECT_DIR:-$(cd "${SCRIPT_DIR}/../.." && pwd)}"
ENV_FILE="${ENV_FILE:-${PROJECT_DIR}/.env}"
BACKUP_BASE_DIR="${BACKUP_BASE_DIR:-/data/backups/project-issue-hub}"
BACKUP_DIR="${BACKUP_DIR:-${BACKUP_BASE_DIR}/minio}"
CONTAINER_NAME="${MINIO_CONTAINER_NAME:-opl-minio}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"
MINIO_DATA_DIR="${MINIO_DATA_DIR:-}"

if [[ -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
fi

mkdir -p "${BACKUP_DIR}"

STAMP="$(date +%F_%H%M%S)"
OUT_FILE="${BACKUP_DIR}/minio_${STAMP}.tar.gz"

if [[ -n "${MINIO_DATA_DIR}" && -d "${MINIO_DATA_DIR}" ]]; then
  echo "[INFO] archive minio host path ${MINIO_DATA_DIR} to ${OUT_FILE}"
  tar -czf "${OUT_FILE}" -C "${MINIO_DATA_DIR}" .
else
  echo "[INFO] archive minio container data from ${CONTAINER_NAME} to ${OUT_FILE}"
  docker run --rm \
    --volumes-from "${CONTAINER_NAME}" \
    -v "${BACKUP_DIR}:/backup" \
    alpine:3.20 \
    sh -c "tar -czf /backup/$(basename "${OUT_FILE}") -C / data"
fi

find "${BACKUP_DIR}" -type f -name "minio_*.tar.gz" -mtime "+${RETENTION_DAYS}" -delete

echo "[INFO] done"
