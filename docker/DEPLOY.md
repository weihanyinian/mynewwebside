# =============================================
# MyWebSide Docker 部署指南
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

## 快速部署

### 0. 本地打包（推荐，适合上传到服务器）

如果你不想在服务器上编译前后端，可以在本地直接打好镜像包再上传：

```powershell
cd E:\mywebside\docker
.\package-for-server.ps1 -Tag v1
```

脚本会在 `docker/dist` 下生成：
- `mywebside-images-v1.tar`（前后端镜像）
- `docker-compose.yml`
- `.env.example`
- `schema.sql`

上传整个 `dist` 目录到服务器后执行：

```bash
cd /opt/mywebside
docker load -i mywebside-images-v1.tar
cp .env.example .env
# 确保 IMAGE_TAG=v1（与本地打包的 -Tag 一致）
sed -i 's/^IMAGE_TAG=.*/IMAGE_TAG=v1/' .env
docker compose up -d
```

### 1. 上传项目到服务器

```bash
# 在服务器上克隆/上传项目
cd /opt/mywebside

# 确保 docker 目录存在
mkdir -p docker
# 将本目录下的所有文件放入 docker/
```

### 2. 配置环境变量

```bash
cd /opt/mywebside/docker
cp .env.example .env
nano .env  # 编辑配置
```

**必须修改的配置：**
- `JWT_SECRET` - 生成随机密钥：
  ```bash
  openssl rand -base64 32
  ```
- `MYSQL_ROOT_PASSWORD` - 数据库 root 密码
- `MYSQL_PASSWORD` - 应用数据库密码
- `ADMIN_INIT_PASSWORD` - 初始管理员密码

### 3. 创建迁移脚本目录（Flyway）

```bash
# 复制现有 SQL 迁移脚本
mkdir -p ../backend/src/main/resources/db/migration
# Flyway 会自动执行该目录下的所有 .sql 文件
```

### 4. 启动服务

```bash
# 构建并启动所有服务（首次需要几分钟）
docker compose up -d --build

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f backend
```

### 5. 验证部署

```bash
# 检查前端
curl http://localhost/health

# 检查后端健康状态
curl http://localhost:8080/actuator/health

# 查看所有服务日志
docker compose logs --tail=100
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
docker exec -it mywebside-backend sh
docker exec -it mywebside-mysql mysql -u root -p
```

## 数据持久化

所有数据存储在 Docker volumes 中：
- `mysql_data` - MySQL 数据文件
- `redis_data` - Redis 持久化文件

备份方法：
```bash
# 备份 MySQL
docker exec mywebside-mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD} blog > backup.sql

# 备份 Redis
docker exec mywebside-redis redis-cli SAVE
docker cp mywebside-redis:/data/dump.rdb ./redis_backup.rdb
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
docker exec mywebside-backend cat /etc/hosts
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
   docker exec -i mywebside-mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} blog < blog_backup.sql
   ```

4. 修改 `application.yml` 中的 Redis 配置（如果之前没用）：
   ```yaml
   app:
     view-counter:
       redis: false  # 关闭 Redis 功能
   ```
