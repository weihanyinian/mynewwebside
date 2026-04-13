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
- 代码高亮：Markdown + Highlight.js
- 响应式、简洁现代风格

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
