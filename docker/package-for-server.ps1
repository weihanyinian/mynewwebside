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
Copy-Item (Join-Path $dockerDir ".env.example") (Join-Path $outputPath ".env.example") -Force

$envEx = Join-Path $outputPath ".env.example"
if (-not (Select-String -Path $envEx -Pattern "^IMAGE_TAG=" -Quiet)) {
    Add-Content $envEx "`nIMAGE_TAG=$Tag`n"
}

Write-Host "Done. Upload folder: $outputPath" -ForegroundColor Green
