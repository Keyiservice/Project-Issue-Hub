#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="${PROJECT_DIR:-$(cd "${SCRIPT_DIR}/../.." && pwd)}"
BACKUP_BASE_DIR="${BACKUP_BASE_DIR:-/data/backups/project-issue-hub}"
BACKUP_DIR="${BACKUP_DIR:-${BACKUP_BASE_DIR}/config}"
RETENTION_DAYS="${RETENTION_DAYS:-30}"
STAMP="$(date +%F_%H%M%S)"
STAGE_DIR="$(mktemp -d)"
OUT_FILE="${BACKUP_DIR}/runtime_config_${STAMP}.tar.gz"

mkdir -p "${BACKUP_DIR}"
trap 'rm -rf "${STAGE_DIR}"' EXIT

copy_if_exists() {
  local src="$1"
  local dest="$2"
  if [[ -e "${src}" ]]; then
    mkdir -p "$(dirname "${dest}")"
    cp -R "${src}" "${dest}"
  fi
}

copy_if_exists "${PROJECT_DIR}/.env" "${STAGE_DIR}/project/.env"
copy_if_exists "${PROJECT_DIR}/docker-compose.yml" "${STAGE_DIR}/project/docker-compose.yml"
copy_if_exists "${PROJECT_DIR}/docs/ops/nginx-project-issue-hub.conf" "${STAGE_DIR}/project/nginx-project-issue-hub.conf"
copy_if_exists "${PROJECT_DIR}/docs/ops/rhel-nginx-intranet.conf" "${STAGE_DIR}/project/rhel-nginx-intranet.conf"
copy_if_exists "${PROJECT_DIR}/docs/ops/project-issue-hub.service" "${STAGE_DIR}/project/project-issue-hub.service"
copy_if_exists "${PROJECT_DIR}/docs/security" "${STAGE_DIR}/project/security-docs"

tar -czf "${OUT_FILE}" -C "${STAGE_DIR}" .
find "${BACKUP_DIR}" -type f -name "runtime_config_*.tar.gz" -mtime "+${RETENTION_DAYS}" -delete

echo "[INFO] runtime config backup created: ${OUT_FILE}"
