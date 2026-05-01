#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if [[ -f .env ]]; then
  sed -i '1s/^\xEF\xBB\xBF//' .env 2>/dev/null || true
  sed -i 's/\r$//' .env 2>/dev/null || true
fi
docker compose up -d --build
docker compose ps
