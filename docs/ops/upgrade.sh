#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="/opt/project-issue-hub"

cd "${PROJECT_DIR}"

echo "[INFO] current branch/tag:"
git rev-parse --abbrev-ref HEAD || true
git describe --tags --always || true

echo "[INFO] pulling latest code"
git pull

echo "[INFO] rebuilding and restarting containers"
docker compose up -d --build

echo "[INFO] current containers"
docker compose ps

echo "[INFO] backend last logs"
docker compose logs --tail=50 backend
