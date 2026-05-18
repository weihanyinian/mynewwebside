#!/usr/bin/env bash
# 阿里云 ECS 首次/更新部署（在仓库根目录执行：bash deploy/aliyun-deploy.sh）
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if ! command -v docker >/dev/null 2>&1; then
  echo "未检测到 Docker，请先安装："
  echo "  Alibaba Cloud Linux 3: sudo dnf install -y docker && sudo systemctl enable --now docker"
  echo "  Ubuntu 22.04: 见 deploy/README.md 第 3 节"
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "未检测到 docker compose 插件，请安装 docker-compose-plugin"
  exit 1
fi

# 生成 deploy/.env 与 deploy/docker-compose.yml（已存在则跳过）
bash "$ROOT/deploy/setup-quick.sh"

ENV_FILE="$ROOT/deploy/.env"
# 尝试用元数据服务写入公网 IP 到 CORS（仅当仍是 localhost 模板时）
if [[ -f "$ENV_FILE" ]]; then
  PUB_IP=""
  PUB_IP="$(curl -fsS --connect-timeout 2 http://100.100.100.200/latest/meta-data/eipv4 2>/dev/null || true)"
  if [[ -z "$PUB_IP" ]]; then
    PUB_IP="$(curl -fsS --connect-timeout 3 https://api.ipify.org 2>/dev/null || true)"
  fi
  if [[ -n "$PUB_IP" ]] && grep -q 'APP_CORS_ALLOWED_ORIGINS=http://localhost' "$ENV_FILE"; then
    sed -i "s|^APP_CORS_ALLOWED_ORIGINS=.*|APP_CORS_ALLOWED_ORIGINS=http://${PUB_IP},http://${PUB_IP}:80|" "$ENV_FILE"
    echo "已根据公网 IP 设置 APP_CORS_ALLOWED_ORIGINS=http://${PUB_IP}"
  fi
fi

chmod 600 "$ENV_FILE" 2>/dev/null || true

echo ""
echo "即将构建并启动（首次约 5–15 分钟）..."
docker compose -f deploy/docker-compose.yml --env-file deploy/.env up -d --build

echo ""
docker compose -f deploy/docker-compose.yml ps
echo ""
echo "部署完成。浏览器访问: http://<你的ECS公网IP>"
echo "若 HTTP_PORT 非 80，请访问 http://<IP>:<HTTP_PORT>"
echo "查看后端日志: docker compose -f deploy/docker-compose.yml logs -f backend"
