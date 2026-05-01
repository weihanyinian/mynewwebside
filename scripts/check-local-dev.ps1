# Local dev port check (music 502: usually 8080 backend down, or 3000 NCM down)
$ErrorActionPreference = "Continue"
Write-Host ""
Write-Host "=== Ports ===" -ForegroundColor Cyan
foreach ($p in @(8080, 3000, 5173)) {
  $ok = (Test-NetConnection -ComputerName 127.0.0.1 -Port $p -WarningAction SilentlyContinue).TcpTestSucceeded
  $label = if ($ok) { "LISTEN" } else { "closed" }
  Write-Host ("  {0} -> {1}" -f $p, $label)
}
Write-Host ""
Write-Host "5173 = Vite (npm run dev)" -ForegroundColor Yellow
Write-Host "8080 = Spring Boot (mvn spring-boot:run); without it -> 502 from Vite proxy" -ForegroundColor Yellow
Write-Host "3000 = Netease NCM API (optional; use docker compose ncm-only below)" -ForegroundColor Yellow
Write-Host ""
Write-Host "Start NCM only (Docker, from repo root):" -ForegroundColor Green
Write-Host "  docker compose -f docker/docker-compose.ncm-only.example.yml up -d"
Write-Host ""
