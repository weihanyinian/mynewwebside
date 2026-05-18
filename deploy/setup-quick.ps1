# 从 example 生成 deploy/.env 与 deploy/docker-compose.yml（二者已 gitignore，可安全含密码）
$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$deploy = Join-Path $root "deploy"

$envExample = Join-Path $deploy ".env.example"
$envTarget = Join-Path $deploy ".env"
$composeExample = Join-Path $deploy "docker-compose.example.yml"
$composeTarget = Join-Path $deploy "docker-compose.yml"

if (-not (Test-Path $envExample)) { throw "Missing $envExample" }
if (-not (Test-Path $composeExample)) { throw "Missing $composeExample" }

if (-not (Test-Path $envTarget)) {
  Copy-Item $envExample $envTarget
  Write-Host "Created deploy/.env from .env.example" -ForegroundColor Green
} else {
  Write-Host "deploy/.env already exists, skipped" -ForegroundColor Yellow
}

if (-not (Test-Path $composeTarget)) {
  Copy-Item $composeExample $composeTarget
  Write-Host "Created deploy/docker-compose.yml from example" -ForegroundColor Green
} else {
  Write-Host "deploy/docker-compose.yml already exists, skipped" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Next (repo root, Docker Desktop running):" -ForegroundColor Cyan
Write-Host "  docker compose -f deploy/docker-compose.yml --env-file deploy/.env up -d --build"
