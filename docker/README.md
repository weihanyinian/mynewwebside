# Docker 部署说明

项目结构：`backend/`（Spring Boot 3 + Flyway + MySQL + Redis）、`frontend/`（Vue 3 + Vite）。  
编排文件在 `docker/`，构建上下文为**仓库根目录**（`..`），以便同时复制 `backend` 与 `frontend`。

## 阿里云 ECS 部署（推荐流程）

### 1. 准备云服务器

- 选购 **ECS**（建议 2 核 4G 及以上），系统可选 **Alibaba Cloud Linux 3** 或 **Ubuntu 22.04**。
- 分配 **公网 IP**（或后续绑定 SLB / 弹性公网 IP）。

### 2. 安全组（非常重要）

在 ECS 控制台 → 本实例安全组 → **入方向** 放行：

| 端口 | 用途 |
|------|------|
| 22 | SSH 维护 |
| 80 | HTTP（本方案对外只开前端 Nginx） |

可选：若以后上 HTTPS，再放行 **443**。  
**不要**对 `0.0.0.0/0` 放行 **3306、6379**（本 compose 未将 MySQL/Redis 映射到宿主机，但若自行映射请务必限制来源 IP）。

### 3. 安装 Docker

**Alibaba Cloud Linux 3：**

```bash
sudo dnf install -y docker
sudo systemctl enable --now docker
sudo usermod -aG docker $USER
# 重新登录 SSH 后 docker 免 sudo
```

**Ubuntu：**

```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
```

安装 Compose 插件（多数新装 Docker 已自带）：

```bash
docker compose version
```

### 4. 拉取代码

```bash
sudo mkdir -p /opt
sudo chown $USER:$USER /opt
cd /opt
git clone <你的仓库地址> mywebsite
cd mywebsite
```

### 5. 配置环境变量

```bash
cp docker/.env.example docker/.env
nano docker/.env
```

必改项：

- `MYSQL_ROOT_PASSWORD`、`MYSQL_PASSWORD`：强密码。
- `JWT_SECRET`：长随机串，例如 `openssl rand -base64 48`。
- `APP_CORS_ALLOWED_ORIGINS`：填写访客在浏览器里实际访问的地址，**逗号分隔、不要空格**。  
  例：`http://47.xxx.xxx.xxx` 或 `https://www.你的域名.com`（若 HTTP/HTTPS 混用，两个都写上）。

可选：`HTTP_PORT` 默认 80；若 80 被占用可改为 `8080` 等。

### 6. 启动

在**仓库根目录** `/opt/mywebsite`：

```bash
docker compose -f docker/docker-compose.yml --env-file docker/.env up -d --build
```

查看状态：

```bash
docker compose -f docker/docker-compose.yml ps
docker compose -f docker/docker-compose.yml logs -f backend
```

浏览器访问：`http://<ECS公网IP>`（或你设置的端口）。

数据库表由后端 **Flyway** 自动迁移，无需再执行 `mysql/schema.sql` 初始化脚本。

### 7. 更新版本

```bash
cd /opt/mywebsite
git pull
docker compose -f docker/docker-compose.yml --env-file docker/.env up -d --build
```

---

## 本地进入 `docker` 目录启动（等价）

```bash
cd docker
cp .env.example .env
# 编辑 .env 后
docker compose up -d --build
```

此时 `docker compose` 默认读取当前目录下的 `docker-compose.yml`，`build.context` 仍为上级目录 `..`。

---

## 离线：本机打镜像再上云

在 **Windows** 项目里：

```powershell
cd docker
.\package-for-server.ps1 -Tag v1
```

将 `docker/dist` 中的 `mywebsite-images-v1.tar`、`docker-compose.yml`、`.env.example` 上传到服务器后：

```bash
docker load -i mywebsite-images-v1.tar
cp .env.example .env
# 将 IMAGE_TAG=v1 写入 .env
docker compose up -d
```

---

## 服务说明

| 容器 | 说明 |
|------|------|
| mywebsite-mysql | MySQL 8，数据卷 `mysql_data` |
| mywebsite-redis | Redis 7，数据卷 `redis_data` |
| mywebsite-ncm-api | 网易云第三方 API，仅容器网络内访问 |
| mywebsite-backend | Spring Boot，`SPRING_PROFILES_ACTIVE=docker` |
| mywebsite-frontend | Nginx 托管前端，`/api` 反代到 backend |

对外仅 **frontend** 映射 `HTTP_PORT`（默认 80）；backend、MySQL、Redis、ncm-api **不映射**到宿主机，减少暴露面。
