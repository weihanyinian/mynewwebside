param(
    [string]$Tag = "latest",
    [string]$OutputDir = "dist"
)

$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Resolve-Path (Join-Path $scriptDir "..")
$dockerDir = Resolve-Path $scriptDir
$outputPath = Join-Path $dockerDir $OutputDir
New-Item -ItemType Directory -Force -Path $outputPath | Out-Null

$backendImage = "mywebsite-backend:$Tag"
$frontendImage = "mywebsite-frontend:$Tag"
$tar = Join-Path $outputPath "mywebsite-images-$Tag.tar"

Write-Host "Build backend: $backendImage" -ForegroundColor Cyan
docker build -f (Join-Path $dockerDir "backend\Dockerfile") -t $backendImage $projectRoot

Write-Host "Build frontend: $frontendImage" -ForegroundColor Cyan
docker build -f (Join-Path $dockerDir "frontend\Dockerfile") -t $frontendImage $projectRoot

Write-Host "Save: $tar" -ForegroundColor Cyan
docker save -o $tar $backendImage $frontendImage

Copy-Item (Join-Path $dockerDir "docker-compose.images.yml") (Join-Path $outputPath "docker-compose.yml") -Force
Copy-Item (Join-Path $dockerDir "README.md") (Join-Path $outputPath "阿里云与Docker完整部署说明.md") -Force

$envGuide = @"
# 在服务器与本目录并排新建 .env（不要提交 Git）
# 完整变量说明与可复制模板见同目录：《阿里云与Docker完整部署说明.md》第五节。

IMAGE_TAG=$Tag
MYSQL_ROOT_PASSWORD=
MYSQL_DATABASE=blog
MYSQL_USER=blog
MYSQL_PASSWORD=
JWT_SECRET=
JWT_EXPIRE_MINUTES=10080
APP_CORS_ALLOWED_ORIGINS=
HTTP_PORT=80
VIEW_COUNTER_REDIS=false
BOOTSTRAP_ADMIN_PASSWORD=
AI_COMPANION_ENABLED=false
AI_COMPANION_API_KEY=
"@
Set-Content -Path (Join-Path $outputPath "dot-env-请填写后重命名为.env.txt") -Value $envGuide -Encoding UTF8

Write-Host "Done. Upload folder: $outputPath" -ForegroundColor Green
Write-Host "服务器上：填好 dot-env-请填写后重命名为.env.txt 为 .env，docker load 后 docker compose up -d" -ForegroundColor Yellow
