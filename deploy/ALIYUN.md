# 阿里云 ECS 部署速查

## 一、安全组（控制台必做）

入方向放行：

| 端口 | 说明 |
|------|------|
| 22 | SSH |
| 80 | 网站（`HTTP_PORT=80` 时） |
| 443 | 可选，上 HTTPS 时 |

**不要**对公网开放 3306、6379、8080。

## 二、安装 Docker（SSH 登录服务器后）

**Alibaba Cloud Linux 3：**

```bash
sudo dnf install -y docker
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"
# 重新 SSH 登录一次
```

**Ubuntu 22.04：** 见 [README.md](./README.md) 第 3 节。

验证：`docker compose version`

## 三、拉代码

```bash
sudo mkdir -p /opt && sudo chown "$USER:$USER" /opt
cd /opt
git clone https://github.com/weihanyinian/mywebsite.git mywebsite
cd mywebsite
```

私有仓库请改用 SSH 地址。

## 四、一键部署

```bash
bash deploy/aliyun-deploy.sh
```

脚本会：生成 `deploy/.env` 与 `deploy/docker-compose.yml`、尝试写入公网 IP 到 CORS、执行 `docker compose up -d --build`。

**生产环境**请编辑 `deploy/.env`，至少修改：

- `MYSQL_ROOT_PASSWORD`、`MYSQL_PASSWORD`
- `JWT_SECRET`（`openssl rand -base64 48`）
- `APP_CORS_ALLOWED_ORIGINS`（与浏览器地址栏一致，如 `http://47.96.xxx.xxx`）

```bash
nano deploy/.env
docker compose -f deploy/docker-compose.yml --env-file deploy/.env up -d --build
```

## 五、验证

```bash
docker compose -f deploy/docker-compose.yml ps
curl -s -o /dev/null -w "%{http_code}\n" http://127.0.0.1/
```

本机浏览器访问：`http://你的ECS公网IP`

## 六、日常更新

```bash
cd /opt/mywebsite
git pull
docker compose -f deploy/docker-compose.yml --env-file deploy/.env up -d --build
```

`deploy/.env` 与 `deploy/docker-compose.yml` 不会被 `git pull` 覆盖。

## 七、常见问题

| 现象 | 处理 |
|------|------|
| 构建很慢 / 拉镜像失败 | 配置 Docker 镜像加速，或在本机 `package-for-server.ps1` 打 tar 离线部署 |
| 浏览器跨域 / 登录失败 | 检查 `APP_CORS_ALLOWED_ORIGINS` 是否与访问 URL 一致 |
| 80 被占用 | `deploy/.env` 设 `HTTP_PORT=8080`，安全组放行 8080 |
| 后端 unhealthy | `docker compose -f deploy/docker-compose.yml logs --tail=200 backend` |

完整说明见 [README.md](./README.md)。
