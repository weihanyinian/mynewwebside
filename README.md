# MyWebSide · 个人技术博客（Spring Boot 3 + Vue 3）

## 目录结构

- `backend/` Spring Boot 3 + MySQL 后端（RESTful）
- `frontend/` Vue 3 + Vite + Element Plus 前端（前台 + 简易后台）
- `mysql/schema.sql` MySQL 建表语句

## 后端能力

- 文章 CRUD（草稿/发布）
- 分类、标签 CRUD
- 单管理员登录（JWT）
- 文章浏览量（访问详情自动 +1）

## 前端能力

- 前台：文章列表 / 详情 / 分类 / 标签 / 搜索
- 后台：登录 / 文章管理 / 发布编辑 / 分类管理 / 标签管理
- **工具栏**（`/tools`）：卡片入口聚合在线 OJ、反应力 / CPS / 番茄钟 / 随机密码 / Base64，以及 **Markmap 思维导图编辑器**（`/tools/markmap`）
- 代码高亮：Markdown + Highlight.js
- 响应式、简洁现代风格

## 工具栏与思维导图（Markmap）

### 访问路径

| 功能 | 路径 |
|------|------|
| 工具栏主页 | `/tools` |
| 在线 OJ | `/tools/oj`（需登录；旧地址 `/oj` 会自动重定向） |
| 思维导图 | `/tools/markmap` |
| 反应力 / CPS / 番茄钟 / 密码 / Base64 | `/tools/reaction`、`/tools/cps` 等 |

小工具为**纯前端**；OJ 判题仍依赖后端与 Judge0 等配置。

### Markmap 编辑器说明

- 左侧 **Markdown**（CodeMirror），右侧 **可交互导图**（拖拽节点、滚轮缩放、点击折叠，由 [markmap](https://github.com/markmap/markmap) 提供能力）。
- 内容自动写入浏览器 **localStorage**（键名 `mywebside-markmap-md`），刷新不丢失。
- 工具条：**保存**（写入本地）、**导出 SVG / PNG / HTML**、**清空**、**重置**为示例文档。
- 代码块与 **LaTeX 公式** 首次渲染时，markmap 可能通过 **jsDelivr** 拉取 KaTeX、Highlight.js 静态资源；内网或离线部署时需保证可访问该 CDN，或自行改造 `markmap-lib` 的资源解析（见 `MarkmapEditor.vue` 内注释与官方文档）。
- 导出 **HTML** 为「当前 SVG 快照 + 隐藏备份的 Markdown 文本」，便于归档；需在支持 SVG 的浏览器中打开查看。

### 部署步骤（静态前端）

1. 在 `frontend` 目录执行 `npm install` 与 `npm run build`。
2. 将 `frontend/dist` 部署到 Nginx / CDN / 对象存储静态网站。
3. 确保站点可正常加载构建产物；若仅部署前端，工具栏内除 OJ 外均可独立使用。

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

后端默认：`http://localhost:8080`

### 3) 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认：`http://localhost:5173`

## 部署建议（腾讯云轻量）

- 后端：`mvn -DskipTests package` 生成 `jar`，用 `java -jar` 启动（建议配合 systemd）
- 前端：`npm run build` 产物在 `frontend/dist`，用 Nginx 静态托管
- 跨域：生产建议统一走 Nginx 反代（同域），或将 `app.cors.allowed-origins` 配置为生产域名
