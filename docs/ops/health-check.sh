#!/usr/bin/env bash
set -euo pipefail

echo "== docker compose ps =="
docker compose ps

echo
echo "== web check =="
curl -I --max-time 10 http://127.0.0.1:8088 || true

echo
echo "== backend swagger check =="
curl -I --max-time 10 http://127.0.0.1:8081/swagger-ui.html || true

echo
echo "== disk usage =="
df -h

echo
echo "== docker disk usage =="
docker system df
