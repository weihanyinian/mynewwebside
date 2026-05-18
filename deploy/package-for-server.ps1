param(
    [string]$Tag = "latest",
    [string]$OutputDir = "dist"
)

$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Resolve-Path (Join-Path $scriptDir "..")
$dockerDir = Join-Path $projectRoot "docker"
$deployDir = Resolve-Path $scriptDir
$outputPath = Join-Path $deployDir $OutputDir
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

Copy-Item (Join-Path $deployDir "docker-compose.images.example.yml") (Join-Path $outputPath "docker-compose.yml") -Force
Copy-Item (Join-Path $deployDir "README.md") (Join-Path $outputPath "阿里云与Docker完整部署说明.md") -Force

$envExample = Join-Path $deployDir ".env.example"
if (Test-Path $envExample) {
    $envContent = Get-Content $envExample -Raw -Encoding UTF8
    $envContent = $envContent -replace '(?m)^IMAGE_TAG=.*$', "IMAGE_TAG=$Tag"
    Set-Content -Path (Join-Path $outputPath ".env") -Value $envContent -Encoding UTF8 -NoNewline
    Write-Host "Copied deploy/.env.example -> dist/.env (edit APP_CORS on server)" -ForegroundColor Green
} else {
    Write-Warning "deploy/.env.example not found; create .env manually on server"
}

Write-Host "Done. Upload folder: $outputPath" -ForegroundColor Green
Write-Host "服务器上：按需改 dist/.env 中 CORS 与密码，docker load 后在该目录 docker compose up -d" -ForegroundColor Yellow
