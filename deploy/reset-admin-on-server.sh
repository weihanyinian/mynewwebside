#!/usr/bin/env bash
# 在服务器仓库根目录执行：bash deploy/reset-admin-on-server.sh
# 用于忘记 admin 密码或首次创建 admin（需 deploy/.env 中已设置 BOOTSTRAP_ADMIN_PASSWORD）
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
ENV_FILE="$ROOT/deploy/.env"
if [[ ! -f "$ENV_FILE" ]]; then
  echo "缺少 deploy/.env"
  exit 1
fi
# shellcheck disable=SC1090
source "$ENV_FILE"
if [[ -z "${BOOTSTRAP_ADMIN_PASSWORD:-}" ]]; then
  echo "请先在 deploy/.env 中设置 BOOTSTRAP_ADMIN_PASSWORD=你的新密码"
  exit 1
fi
export APP_BOOTSTRAP_ADMIN_SYNC_ON_STARTUP=true
echo "重启 backend，将用 BOOTSTRAP_ADMIN_PASSWORD 创建或覆盖 admin 密码…"
docker compose -f deploy/docker-compose.yml --env-file deploy/.env up -d backend
sleep 8
docker compose -f deploy/docker-compose.yml logs --tail=30 backend
echo ""
echo "请用 用户名 admin + deploy/.env 中的 BOOTSTRAP_ADMIN_PASSWORD 登录。"
echo "登录成功后，把 deploy/.env 中 APP_BOOTSTRAP_ADMIN_SYNC_ON_STARTUP 删掉或设为 false，再执行："
echo "  docker compose -f deploy/docker-compose.yml --env-file deploy/.env up -d backend"
