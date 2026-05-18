#!/usr/bin/env bash
# 从 example 生成 deploy/.env 与 deploy/docker-compose.yml（已 gitignore）
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DEPLOY="$ROOT/deploy"

[[ -f "$DEPLOY/.env.example" ]] || { echo "Missing deploy/.env.example"; exit 1; }
[[ -f "$DEPLOY/docker-compose.example.yml" ]] || { echo "Missing deploy/docker-compose.example.yml"; exit 1; }

if [[ ! -f "$DEPLOY/.env" ]]; then
  cp "$DEPLOY/.env.example" "$DEPLOY/.env"
  echo "Created deploy/.env"
else
  echo "deploy/.env already exists, skipped"
fi

if [[ ! -f "$DEPLOY/docker-compose.yml" ]]; then
  cp "$DEPLOY/docker-compose.example.yml" "$DEPLOY/docker-compose.yml"
  echo "Created deploy/docker-compose.yml"
else
  echo "deploy/docker-compose.yml already exists, skipped"
fi

echo ""
echo "Next (repo root):"
echo "  docker compose -f deploy/docker-compose.yml --env-file deploy/.env up -d --build"
