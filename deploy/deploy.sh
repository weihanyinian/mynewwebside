#!/usr/bin/env bash
set -euo pipefail
# 在仓库根目录执行构建与启动（与 deploy/README.md 一致）
root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"
if [[ -f deploy/.env ]]; then
  sed -i '1s/^\xEF\xBB\xBF//' deploy/.env 2>/dev/null || true
  sed -i 's/\r$//' deploy/.env 2>/dev/null || true
fi
compose="deploy/docker-compose.yml"
if [[ ! -f "$compose" ]]; then
  echo "未找到 $compose，从 deploy/docker-compose.example.yml 复制（可按服务器环境再改，该文件不受 Git 跟踪）。"
  cp deploy/docker-compose.example.yml "$compose"
fi
docker compose -f "$compose" --env-file deploy/.env up -d --build
docker compose -f "$compose" ps
