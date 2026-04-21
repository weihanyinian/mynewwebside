# 用法（在 backend 目录外执行也可）:
#   powershell -ExecutionPolicy Bypass -File scripts/init_mysql_blog.ps1
# 按提示输入 MySQL root 密码；用于创建 blog 库（utf8mb4）

$mysql = "mysql"
$candidates = @(
  "E:\mysql-9.1.0-winx64\bin\mysql.exe",
  "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
)
foreach ($c in $candidates) {
  if (Test-Path $c) { $mysql = $c; break }
}

$pw = Read-Host "MySQL root 密码" -AsSecureString
$BSTR = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($pw)
$plain = [Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)

$sql = "CREATE DATABASE IF NOT EXISTS blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; SHOW DATABASES LIKE 'blog';"
& $mysql -u root "-p$plain" -e $sql
if ($LASTEXITCODE -ne 0) {
  Write-Host "建库失败：请检查 mysql 路径、用户名密码，或手动执行 init_mysql_blog.sql" -ForegroundColor Red
  exit 1
}
Write-Host "blog 数据库已就绪。请将 application.yml 中 MYSQL_PASSWORD 设为同一密码，并去掉 h2 profile 启动后端。" -ForegroundColor Green
