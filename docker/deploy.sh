#!/usr/bin/env bash
set -euo pipefail
# 在仓库根目录执行构建与启动（与 docker/README.md 一致）
root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"
if [[ -f docker/.env ]]; then
  sed -i '1s/^\xEF\xBB\xBF//' docker/.env 2>/dev/null || true
  sed -i 's/\r$//' docker/.env 2>/dev/null || true
fi
docker compose -f docker/docker-compose.yml --env-file docker/.env up -d --build
docker compose -f docker/docker-compose.yml ps
