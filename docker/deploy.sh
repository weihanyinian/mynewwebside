#!/usr/bin/env bash
set -euo pipefail
# 在仓库根目录执行构建与启动（与 docker/README.md 一致）
root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"
if [[ -f docker/.env ]]; then
  sed -i '1s/^\xEF\xBB\xBF//' docker/.env 2>/dev/null || true
  sed -i 's/\r$//' docker/.env 2>/dev/null || true
fi
compose="docker/docker-compose.yml"
if [[ ! -f "$compose" ]]; then
  echo "未找到 $compose，从 docker-compose.example.yml 复制（可按服务器环境再改，该文件不受 Git 跟踪）。"
  cp docker/docker-compose.example.yml "$compose"
fi
docker compose -f "$compose" --env-file docker/.env up -d --build
docker compose -f "$compose" ps
