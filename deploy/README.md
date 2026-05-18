# 个人博客全栈：阿里云 ECS + Docker 一次性部署手册

面向本仓库当前结构：**`backend/`**（Spring Boot 3、Flyway、MySQL、Redis）、**`frontend/`**（Vue 3、Vite）。  
**`docker/`** 在 Git 中**仅含**镜像构建：`docker/backend/Dockerfile`、`docker/frontend/Dockerfile`（及 `nginx.conf`），**无 yml、无 env**。编排模板、部署脚本与本说明在 **`deploy/`**；镜像构建上下文仍为**仓库根目录**（compose 里 `context: ..`）。**Git 只提交 `deploy/*.example.yml` 模板**；由模板生成的 `deploy/docker-compose*.yml` 与 **`deploy/.env`** 已 **`.gitignore`**，`git pull` 不会覆盖现场配置。

---

## 目录

1. [架构与端口](#1-架构与端口)  
2. [ECS 与安全组](#2-ecs-与安全组)  
3. [安装 Docker](#3-安装-docker)  
4. [获取代码](#4-获取代码)  
5. [创建 `deploy/.env`（全文模板，必做）](#5-创建-deployenv全文模板必做)  
6. [首次启动](#6-首次启动)  
7. [验证是否成功](#7-验证是否成功)  
8. [日常更新](#8-日常更新)  
9. [离线部署（本机打镜像）](#9-离线部署本机打镜像)  
10. [故障排查](#10-故障排查)  

---

## 1. 架构与端口

| 组件 | 容器名 | 说明 |
|------|--------|------|
| MySQL 8 | `mywebsite-mysql` | 业务库，数据在卷 `mysql_data` |
| Redis 7 | `mywebsite-redis` | 缓存等，卷 `redis_data` |
| 网易云增强 API | `mywebsite-ncm-api` | 镜像 `moefurina/ncm-api`（[@neteasecloudmusicapienhanced/api](https://www.npmjs.com/package/@neteasecloudmusicapienhanced/api)），仅内网 |
| QQ 音乐 API | `mywebsite-qq-music-api` | 自建镜像（[sansenjian/qq-music-api](https://github.com/sansenjian/qq-music-api)，[API 文档](https://sansenjian.github.io/qq-music-api/api/)），默认 **3200**，仅内网 |
| 后端 | `mywebsite-backend` | `SPRING_PROFILES_ACTIVE=docker`，**不映射宿主机端口** |
| 前端 | `mywebsite-frontend` | Nginx 静态资源 + `/api` 反代到后端 |

对外只暴露 **`HTTP_PORT`（默认 80）→ 前端容器**。访客浏览器只访问 `http://公网IP` 或域名即可；**不要**把 MySQL、Redis、8080 开到公网安全组。

数据库表由后端 **Flyway** 自动迁移，**不需要**在服务器上再执行 `mysql/schema.sql`。

---

## 2. ECS 与安全组

1. 购买 **阿里云 ECS**（建议 2 核 4G 及以上），系统可选 **Alibaba Cloud Linux 3** 或 **Ubuntu 22.04**，分配**公网 IP**。  
2. 控制台 → 实例 → **安全组** → **入方向** 添加规则：

| 协议 | 端口 | 授权对象 | 说明 |
|------|------|----------|------|
| TCP | 22 | 你的办公网 IP / 谨慎使用 0.0.0.0/0 | SSH |
| TCP | 80 | 0.0.0.0/0 | HTTP 站点 |
| TCP | 443 | 0.0.0.0/0 | 可选，上 HTTPS 时再配 |

**不要**对 `0.0.0.0/0` 放行 3306、6379、8080（本方案默认也未映射这些端口到宿主机）。

---

## 3. 安装 Docker

**Alibaba Cloud Linux 3：**

```bash
sudo dnf install -y docker
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"
```

**Ubuntu 22.04：**

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo usermod -aG docker "$USER"
```

验证（**新开一个 SSH 会话**或重新登录后再执行，避免组权限未生效）：

```bash
docker version
docker compose version
```

若仍提示权限不足，可暂时：`sudo docker compose version`。

---

## 4. 获取代码

```bash
sudo mkdir -p /opt
sudo chown "$USER:$USER" /opt
cd /opt
git clone https://github.com/weihanyinian/mywebsite.git mywebsite
cd mywebsite
```

若使用私有仓库，请改为 SSH 地址或配置 `git credential`。

### 4.1 Compose 模板（首次必做，避免 pull 覆盖服务器编排）

仓库内仅跟踪 **`deploy/docker-compose.example.yml`**（以及 `docker-compose.images.example.yml`、`docker-compose.ncm-only.example.yml`）。请在仓库根**一次性**生成实际文件（之后可按服务器环境随意修改，**这些文件名已被 Git 忽略**）：

```bash
cd /opt/mywebsite
cp deploy/docker-compose.example.yml deploy/docker-compose.yml
```

使用「预构建镜像」流程时，可复制 `deploy/docker-compose.images.example.yml` → `deploy/docker-compose.images.yml`。本地只起 NCM 可直接执行  
`docker compose -f deploy/docker-compose.ncm-only.example.yml up -d`，不必复制。

---

## 5. 创建 `deploy/.env`（全文模板，必做）

仓库已提供 **`deploy/.env.example`**（含默认密码与 JWT，可提交 Git）。在服务器上生成**不提交**的 `deploy/.env`：

```bash
cd /opt/mywebsite
cp deploy/.env.example deploy/.env
# 生产务必修改 MYSQL_*、JWT_SECRET、APP_CORS_ALLOWED_ORIGINS
chmod 600 deploy/.env
```

或使用一键脚本（同时生成 `deploy/docker-compose.yml`）：

```bash
bash deploy/setup-quick.sh
```

也可手动新建 `deploy/.env`，结构与下面**完全一致**，并把**所有必须修改项**换成你自己的值：

```bash
nano deploy/.env
```

**将下面整个代码框内的内容粘贴进编辑器**（若已用 `.env.example` 可跳过），保存退出（nano：`Ctrl+O` 回车，`Ctrl+X`）。

```dotenv
# ============ MySQL（官方镜像会创建库 + 用户）============
MYSQL_ROOT_PASSWORD=在这里写数据库root强密码
MYSQL_DATABASE=blog
MYSQL_USER=blog
MYSQL_PASSWORD=在这里写应用用户强密码与上面不同

# ============ JWT（必须足够长；可在服务器执行: openssl rand -base64 48）============
JWT_SECRET=在这里粘贴openssl生成的随机串至少32字符

JWT_EXPIRE_MINUTES=10080

# ============ CORS：浏览器访问你站点时看到的「协议+主机+端口」逗号分隔不要空格 ============
# 例：仅 IP:   http://47.96.xxx.xxx
# 例：仅域名: https://www.yourdomain.com
# 例：都要:   http://47.96.xxx.xxx,https://www.yourdomain.com
APP_CORS_ALLOWED_ORIGINS=http://你的ECS公网IP或域名

# ============ 宿主机映射到前端的端口（默认 80；被占用可改 8080）============
HTTP_PORT=80

# ============ 可选：阅读量走 Redis 写库缓冲（一般 false）============
VIEW_COUNTER_REDIS=false

# ============ 可选：首次引导管理员密码（见后端 bootstrap 说明，不需要可留空）============
BOOTSTRAP_ADMIN_PASSWORD=

# ============ 可选：看板娘 AI ============
AI_COMPANION_ENABLED=false
AI_COMPANION_API_KEY=

# ============ 仅在使用「镜像 tar + docker-compose.images.example.yml」时需要 ============
IMAGE_TAG=latest
```

**必填检查清单（少一项都会导致启动失败或浏览器跨域失败）：**

1. `MYSQL_ROOT_PASSWORD`、`MYSQL_PASSWORD`：已改为强密码。  
2. `JWT_SECRET`：已在服务器执行 `openssl rand -base64 48`，粘贴到此处。  
3. `APP_CORS_ALLOWED_ORIGINS`：与你在浏览器地址栏访问**完全一致**的来源列表（含 `http://` 或 `https://`，**无尾斜杠**习惯上也可不带路径）。

**权限（可选，防止同机其他用户读到密码）：**

```bash
chmod 600 deploy/.env
```

---

## 6. 首次启动

确认已完成 **4.1 节**（已存在 `deploy/docker-compose.yml`）。在**仓库根目录**执行（`/opt/mywebsite`）：

```bash
cd /opt/mywebsite
docker compose -f deploy/docker-compose.yml --env-file deploy/.env up -d --build
```

首次构建可能需要 **数分钟**（拉基础镜像、Maven、npm）。完成后：

```bash
docker compose -f deploy/docker-compose.yml ps
```

期望所有服务为 `running` 或 `healthy`（刚启动时 `backend` 可能短暂 `starting`，可再等 1～2 分钟）。

查看后端日志：

```bash
docker compose -f deploy/docker-compose.yml logs -f backend
```

看到 Spring 启动完成、无 Flyway 报错即可 `Ctrl+C` 退出日志跟随。

---

## 7. 验证是否成功

在**你自己的电脑浏览器**访问：

```text
http://你的ECS公网IP
```

若 `HTTP_PORT` 改为例如 `8080`，则访问 `http://公网IP:8080`。

在服务器本机可执行：

```bash
curl -sI "http://127.0.0.1:${HTTP_PORT:-80}/" | head -5
```

（若 `.env` 里 `HTTP_PORT` 非 80，请先 `set -a && source deploy/.env && set +a` 再 curl，或直接把 URL 里的端口改成你设置的数字。）

---

## 8. 日常更新

```bash
cd /opt/mywebsite
git pull
docker compose -f deploy/docker-compose.yml --env-file deploy/.env up -d --build
```

`deploy/docker-compose.yml` 为服务器本地文件，**不会被** `git pull` 覆盖。若仓库内 `docker-compose.example.yml` 有重要变更，可用 `diff deploy/docker-compose.example.yml deploy/docker-compose.yml` 自行合并。

---

## 9. 离线部署（本机打镜像）

适用于服务器**不能访问 Docker Hub / 构建慢**的情况：在 **Windows** 开发机安装 Docker Desktop，在项目里执行：

```powershell
cd deploy
.\package-for-server.ps1 -Tag v1
```

会在 `deploy/dist` 生成：

- `mywebsite-images-v1.tar`
- `docker-compose.yml`（由 `docker-compose.images.example.yml` 复制生成）

将整个 `dist` 目录上传到服务器某目录（例如 `/opt/mywebsite-dist`）。目录内会有：

- `mywebsite-images-v1.tar`
- `docker-compose.yml`
- `阿里云与Docker完整部署说明.md`（与本仓库 `deploy/README.md` 相同）
- `dot-env-请填写后重命名为.env.txt`（骨架，**必须**按说明改成真正的 `.env`）

在服务器执行：

```bash
cd /opt/mywebsite-dist
docker load -i mywebsite-images-v1.tar
```

在**同一目录**（与 `docker-compose.yml` 同级）创建 **`.env`**：内容与[第 5 节](#5-创建-deployenv全文模板必做)**完全相同**，并**必须包含**（与打包标签一致）：

```dotenv
IMAGE_TAG=v1
```

可将 `dot-env-请填写后重命名为.env.txt` 复制为 `.env` 再逐项填写。启动：

```bash
docker compose up -d
```

Compose 会自动读取当前目录下的 `.env`。若你坚持用显式参数：

```bash
docker compose --env-file .env up -d
```

确认 `docker-compose.yml` 中镜像为 `mywebsite-backend:v1`、`mywebsite-frontend:v1`，与 `IMAGE_TAG=v1` 一致。

---

## 10. 故障排查

**1）`docker compose` 报 `JWT_SECRET` / `MYSQL_PASSWORD` / `APP_CORS_ALLOWED_ORIGINS` 未设置**  
→ 说明 `deploy/.env` 未加载或变量名拼写错误。确认使用 `--env-file deploy/.env`，且变量名与第 5 节一致。

**2）浏览器能打开页面，但登录或 API 报 CORS**  
→ `APP_CORS_ALLOWED_ORIGINS` 必须包含浏览器地址栏的**完整来源**（含协议与端口）。

**3）后端一直重启**  
```bash
docker compose -f deploy/docker-compose.yml logs --tail=200 backend
```  
常见原因：数据库密码错误、MySQL 未就绪（首次多等一会）、Flyway 与旧数据冲突（新装 ECS 一般无此问题）。

**4）80 端口被占用**  
在 `deploy/.env` 中改 `HTTP_PORT=8080`，安全组放行对应端口，访问 `http://IP:8080`。

**5）彻底重来（会删库，慎用）**  
```bash
cd /opt/mywebsite
docker compose -f deploy/docker-compose.yml --env-file deploy/.env down -v
docker compose -f deploy/docker-compose.yml --env-file deploy/.env up -d --build
```

---

## 等价启动方式（可选）

也可先 `cd deploy` 再写相对路径，但推荐始终在**仓库根**使用  
`-f deploy/docker-compose.yml --env-file deploy/.env`，路径最清晰。

---

以上为从零到可访问的**完整**步骤。`.env` 必须手写，不按 example 复制；**Compose 编排**则使用仓库内的 `*.example.yml` 生成本地 `docker-compose*.yml`（后者不提交 Git，以免覆盖服务器配置）。
