#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")"

echo "=== Website Docker 部署（在 docker 目录执行）==="

if [[ -f .env ]]; then
  sed -i '1s/^\xEF\xBB\xBF//' .env 2>/dev/null || true
  sed -i 's/\r$//' .env 2>/dev/null || true
fi

echo "停止旧容器..."
docker compose down

echo "构建并启动（首次较慢）..."
docker compose up -d --build

echo "容器状态："
docker compose ps

echo "完成。查看后端日志: docker compose logs -f backend"
echo "仅当 Flyway 与已有库冲突需手工修复时，才在 MySQL 容器内执行 fix-flyway.sql（会改 flyway 历史表，慎用）。"
