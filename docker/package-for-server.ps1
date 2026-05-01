param(
    [string]$Tag = "latest",
    [string]$OutputDir = "dist"
)

$ErrorActionPreference = "Stop"

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Resolve-Path (Join-Path $scriptDir "..")
$dockerDir = Resolve-Path $scriptDir
$outputPath = Join-Path $dockerDir $OutputDir

$backendImage = "website-backend:$Tag"
$frontendImage = "website-frontend:$Tag"
$archiveName = "website-images-$Tag.tar"
$archivePath = Join-Path $outputPath $archiveName

Write-Step "准备输出目录: $outputPath"
New-Item -ItemType Directory -Force -Path $outputPath | Out-Null

Write-Step "构建后端镜像: $backendImage"
docker build -f (Join-Path $dockerDir "backend/Dockerfile") -t $backendImage $projectRoot

Write-Step "构建前端镜像: $frontendImage"
docker build -f (Join-Path $dockerDir "frontend/Dockerfile") -t $frontendImage $projectRoot

Write-Step "导出镜像到归档: $archivePath"
docker save -o $archivePath $backendImage $frontendImage

Write-Step "复制部署所需文件"
Copy-Item (Join-Path $dockerDir "docker-compose.images.yml") (Join-Path $outputPath "docker-compose.yml") -Force
Copy-Item (Join-Path $dockerDir ".env.example") (Join-Path $outputPath ".env.example") -Force
Copy-Item (Join-Path $projectRoot "mysql/schema.sql") (Join-Path $outputPath "schema.sql") -Force

$envOutput = Join-Path $outputPath ".env.example"
if (-not (Select-String -Path $envOutput -Pattern "^IMAGE_TAG=" -Quiet)) {
    Add-Content -Path $envOutput -Value ""
    Add-Content -Path $envOutput -Value "# 镜像版本标签（需与导出时的 -Tag 一致）"
    Add-Content -Path $envOutput -Value "IMAGE_TAG=$Tag"
}

Write-Step "打包完成"
Write-Host "输出目录: $outputPath" -ForegroundColor Green
Write-Host "镜像归档: $archivePath" -ForegroundColor Green
Write-Host ""
Write-Host 'Next steps:' -ForegroundColor Yellow
Write-Host "1) Upload directory: $outputPath"
Write-Host "2) On server run: docker load -i $archiveName"
Write-Host '3) Copy .env.example to .env and edit secrets'
Write-Host '4) Run: docker compose up -d'
