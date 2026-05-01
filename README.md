# Website · 个人技术博客（Spring Boot 3 + Vue 3）

## 目录结构

- `backend/` Spring Boot 3 + MySQL 后端（RESTful）
- `frontend/` Vue 3 + Vite + Element Plus 前端（前台 + 简易后台）
- `docker/` 仅镜像构建（`backend`、`frontend` 的 Dockerfile 等），**无 compose / .env**；云上编排与变量模板见 **`deploy/README.md`**
- `mysql/schema.sql` MySQL 建表语句

## 后端能力

- 文章 CRUD（草稿/发布）
- 分类、标签 CRUD
- 单管理员登录（JWT）
- 文章浏览量（访问详情自动 +1）

## 前端能力

- 前台：文章列表 / 详情 / 分类 / 标签 / 搜索
- 后台：登录 / 文章管理 / 发布编辑 / 分类管理 / 标签管理
- **工具栏**（`/tools`）：卡片入口聚合在线 OJ、反应力 / CPS / 番茄钟 / MBTI
- 代码高亮：Markdown + Highlight.js
- 响应式、简洁现代风格

## 工具栏路径

| 功能 | 路径 |
|------|------|
| 工具栏主页 | `/tools` |
| 在线 OJ | `/tools/oj`（需登录；旧地址 `/oj` 会自动重定向） |
| 反应力 / CPS / 番茄钟 / MBTI | `/tools/reaction`、`/tools/cps`、`/tools/pomodoro`、`/tools/mbti` |

小工具多为**纯前端**；**在线 OJ** 判题依赖后端与 MySQL。

### 重复文件排查

在仓库根目录执行：`node scripts/find-duplicate-files.mjs`（可选传入要扫描的根目录路径）。输出按内容哈希分组的重复路径，删除前请人工确认。

### 部署步骤（静态前端）

1. 在 `frontend` 目录执行 `npm install` 与 `npm run build`。
2. 将 `frontend/dist` 部署到 Nginx / CDN / 对象存储静态网站。
3. 确保站点可正常加载构建产物；若仅部署前端，工具栏内除 OJ 外均可独立使用。

## 网易云音乐 API 接入说明（增强版）

### 1) 启动第三方 API 服务（本机 Node 方式）

```bash
npm i -g @neteasecloudmusicapienhanced/api
netease-cloud-music-api-enhanced
```

默认端口 `3000`，可通过环境变量指定：

```bash
PORT=3001 netease-cloud-music-api-enhanced
```

Windows PowerShell：

```powershell
$env:PORT=3001
netease-cloud-music-api-enhanced
```

### 2) 启动第三方 API 服务（Docker 方式，增强版镜像）

与 npm 包 [@neteasecloudmusicapienhanced/api](https://www.npmjs.com/package/@neteasecloudmusicapienhanced/api) 对应：

```bash
docker pull moefurina/ncm-api:latest
docker run -d --name ncm-api -p 3000:3000 \
  -e http_proxy= -e https_proxy= -e HTTP_PROXY= -e HTTPS_PROXY= -e no_proxy=* -e NO_PROXY=* \
  moefurina/ncm-api:latest
```

（若宿主设置了不可用代理，可按文档清空上述变量；也可直接 `docker compose` 使用仓库内 `ncm-api` 服务。）

健康检查（任选其一）：

```bash
curl "http://127.0.0.1:3000/login/status"
curl "http://127.0.0.1:3000/song/url?id=33894312"
```

**本地 `localhost:5173` 音乐组件报 502：** 多数是 **Spring Boot 未在 8080 启动**（Vite 反代失败）或 **3000 未起**（可只起 NCM：`docker compose -f deploy/docker-compose.ncm-only.example.yml up -d`）。可在项目根运行 `powershell -File scripts/check-local-dev.ps1` 查看 8080/3000/5173 端口。后端对 `127.0.0.1:3000` 不可达时会尝试 **公网回退**（见 `application.example.yml` 中 `local-fallback-enabled`），但 **必须先保证 8080 后端已运行**。

### 3) 后端配置（Spring Boot）

复制配置模板后，重点确认：

- `netease.proxy.base-url`：指向你启动的增强 API 地址（如 `http://127.0.0.1:3000`）；站内绑定账号走 `/api/music/login`（需本站 JWT），独立页 `Login.vue` 走 **`/api/ncm/*`**（已 `permitAll`，与 npm **`login_cellphone`** 对齐：表单 POST、`countrycode` 默认 86、响应体 **`cookie`** 会写入服务端 Session）。
- `ncm.base-url`：与上相同根地址，供 `/api/ncm` 代理使用。
- `netease.proxy.default-playlist-id`：默认歌单
- `netease.proxy.default-br`：默认音质码率（128000/192000/320000/999000）
- `netease.proxy.retry-count`：上游失败重试次数
- `app.cors.allowed-origins`：前端域名

### 4) 对接架构

- 前端只调用本站后端接口：
  - 登录态接口：`/api/music/*`
  - 公共接口：`/api/public/music/*`
- 后端再代理到第三方增强 API，不在浏览器直连第三方服务。

### 5) 功能验证清单

1. 登录站点账号，进入 `/music` 页面。
2. 绑定网易云账号（手机号 + 密码）后，`状态` 应显示已绑定昵称。
3. 检查歌单、喜欢、最近播放是否可加载。
4. 点击任意歌曲：
   - 播放/暂停正常
   - 上一首/下一首正常
   - 进度条可拖动跳转
   - 音量滑块可调且刷新后保持
   - 歌词滚动与高亮正常
5. 切换播放模式（顺序/单曲循环/随机）并验证边界行为。
6. 选一首无版权曲目，确认自动跳过并有提示，不崩溃。
7. 停掉第三方 API 或后端，确认前端提示“加载失败/服务不可用”。

## 本地运行

### 1) MySQL 初始化

执行：

- `mysql/schema.sql`

默认数据库名 `blog`。

### 2) 启动后端

先复制一份本地配置：

```bash
cp backend/src/main/resources/application.example.yml backend/src/main/resources/application.yml
```

Windows PowerShell：

```powershell
Copy-Item backend/src/main/resources/application.example.yml backend/src/main/resources/application.yml
```

然后修改 `backend/src/main/resources/application.yml`：

- `spring.datasource.username/password`
- `app.admin.username/password`（演示用，生产建议使用环境变量）
- `app.jwt.secret`（生产必须替换）
- `app.cors.allowed-origins`（前端地址）

启动：

```bash
cd backend
mvn spring-boot:run
```

若出现 **`Unresolved compilation problem`**：多为 IDE 把错误 class 写进了 `target/classes`，请先 **`mvn clean package`** 再 **`java -jar target/blog-backend-0.1.0.jar`**，或只用 Maven 编译运行。

若 Flyway 报 **`Migration checksum mismatch`**（迁移文件曾改动）：在 MySQL 的 `blog` 库执行 repair，或把历史表中的 `checksum` 改成与当前 `db/migration` 文件一致（控制台日志里会打印 `Resolved locally` 的期望值），也可用 `mvn -DskipTests flyway:repair`（需在 `pom.xml` 配置 Flyway 插件并传入数据源）。

后端默认：`http://localhost:8080`

### 3) 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认：`http://localhost:5173`

