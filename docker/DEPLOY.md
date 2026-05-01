# =============================================
# Website Docker 部署指南
# =============================================

## 前置条件

服务器需安装 Docker 和 Docker Compose：
```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com | sh
sudo systemctl enable docker
sudo systemctl start docker

# 安装 Docker Compose (独立工具)
sudo apt update && sudo apt install docker-compose-v2

# 验证安装
docker --version
docker compose version
```

## 从 GitHub 在云服务器部署（推荐）

```bash
sudo apt update && sudo apt install -y git
git clone <你的仓库 HTTPS 或 SSH 地址> /opt/website
cd /opt/website/docker
cp .env.example .env
nano .env   # 填写 MYSQL_*、JWT_SECRET、CORS_ALLOWED_ORIGINS 等
docker compose up -d --build
```

- 前端静态站：`http://服务器IP:80`
- 统一入口（网关）：`http://服务器IP:88`（`/api` 走后端，`/` 走前端）
- 后端直连调试：`http://服务器IP:8080/actuator/health`（生产建议防火墙只放行 80/443）

数据库表结构由后端 JAR 内 **Flyway** 迁移自动执行，**不要**再挂载 `mysql/schema.sql` 到 `docker-entrypoint-initdb.d`，否则易与迁移脚本重复冲突。

## 本地修改后推送到 GitHub

在开发机项目根目录：

```bash
git add -A
git status   # 确认没有 .env / docker/.env
git commit -m "chore: docker 部署配置"
git push origin main
```

云服务器更新：

```bash
cd /opt/website
git pull
cd docker
docker compose up -d --build
```

## 本地打包镜像再上传（无需在服务器编译）

```powershell
cd <项目根>\docker
.\package-for-server.ps1 -Tag v1
```

会在 `docker/dist` 生成 `website-images-v1.tar`、`docker-compose.yml`、`.env.example`。上传到服务器后：

```bash
docker load -i website-images-v1.tar
cp .env.example .env
# 与打包标签一致
sed -i 's/^IMAGE_TAG=.*/IMAGE_TAG=v1/' .env
docker compose up -d
```

## 配置说明（`.env`）

**必须修改：**

- `JWT_SECRET`：`openssl rand -base64 32`
- `MYSQL_ROOT_PASSWORD`、`MYSQL_PASSWORD`
- `CORS_ALLOWED_ORIGINS`：含你的公网访问来源，例如 `http://你的域名:88,https://你的域名`

可选：`ADMIN_INIT_PASSWORD`（若使用后端 bootstrap 初始化管理员）。

## 启动与验证

```bash
cd /opt/website/docker
docker compose up -d --build
docker compose ps
docker compose logs -f backend
```

```bash
curl -s http://127.0.0.1:88/health
curl -s http://127.0.0.1:8080/actuator/health
```

## 常用命令

```bash
# 停止服务
docker compose down

# 重启服务
docker compose restart backend

# 更新部署（拉取最新代码后）
docker compose up -d --build

# 查看资源使用
docker stats

# 进入容器调试
docker exec -it website-backend sh
docker exec -it website-mysql mysql -u root -p
```

## 数据持久化

所有数据存储在 Docker volumes 中：
- `mysql_data` - MySQL 数据文件
- `redis_data` - Redis 持久化文件

备份方法：
```bash
# 备份 MySQL
docker exec website-mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD} blog > backup.sql

# 备份 Redis
docker exec website-redis redis-cli SAVE
docker cp website-redis:/data/dump.rdb ./redis_backup.rdb
```

## 生产环境优化

### 1. 使用 Nginx 反向代理（可选）

如果需要域名 HTTPS，可以在外层再加一层 Nginx 或 Caddy：

```yaml
# docker-compose.yml 添加
proxy:
  image: nginx:1.25-alpine
  volumes:
    - ./proxy/Caddyfile:/etc/caddy/Caddyfile
    - ./proxy/ssl:/etc/caddy/ssl
  ports:
    - "80:80"
    - "443:443"
```

### 2. 资源限制

2核2G 服务器建议添加内存限制：

```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          memory: 512M
        reservations:
          memory: 256M
```

### 3. 日志轮转

```bash
# /etc/docker/daemon.json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
```

## 故障排查

```bash
# 1. MySQL 无法启动
docker compose logs mysql
# 可能是数据卷权限问题，尝试：
docker compose down -v  # ⚠️ 会删除数据
docker compose up -d

# 2. 后端连接数据库失败
docker exec website-backend cat /etc/hosts
# 确认 mysql hostname 可解析

# 3. 前端 502 Bad Gateway
docker compose logs frontend
docker compose logs backend
# 检查后端是否正常启动

# 4. 端口被占用
sudo lsof -i :80
sudo lsof -i :3306
```

## 从宝塔迁移到纯 Docker

1. 导出原有数据库：
   ```bash
   mysqldump -u root -p blog > blog_backup.sql
   ```

2. 停止宝塔上的 Java 进程：
   ```bash
   # 查看进程
   ps aux | grep java
   # 停止
   pkill -f blog-backend
   ```

3. 在 Docker 中导入数据：
   ```bash
   docker exec -i website-mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} blog < blog_backup.sql
   ```

4. 修改 `application.yml` 中的 Redis 配置（如果之前没用）：
   ```yaml
   app:
     view-counter:
       redis: false  # 关闭 Redis 功能
   ```
