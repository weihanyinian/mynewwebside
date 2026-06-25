# 维寒一念的小站 — 全面重构计划

> 目标：模块化架构 + 完整前后端分离 + 2C2G 服务器适配 + 移动端适配  
> 执行日期：2026-06-25

---

## 阶段一：后端模块化重构

### 新目录结构
```
backend/src/main/java/com/weihanyinian/website/
├── WebsiteApplication.java
├── module/
│   ├── blog/              文章/分类/标签
│   │   ├── controller/    ArticleController.java
│   │   ├── service/       ArticleService.java
│   │   ├── entity/        Article.java, Category.java, Tag.java
│   │   └── repository/    ArticleRepository, CategoryRepository, TagRepository
│   ├── guestbook/         留言板
│   │   ├── controller/    GuestbookController.java
│   │   ├── service/       GuestbookService.java
│   │   ├── entity/        Guestbook.java
│   │   └── repository/    GuestbookRepository
│   ├── comment/           评论
│   │   ├── controller/    CommentController.java
│   │   ├── service/       CommentService.java
│   │   ├── entity/        Comment.java
│   │   └── repository/    CommentRepository
│   ├── music/             网易云音乐
│   │   ├── controller/    MusicController.java
│   │   ├── service/       MusicService.java, NcmApiClient.java
│   │   └── dto/           SongDto, PlaylistDto, LyricDto, SearchResultDto
│   ├── ai/                AI 伴聊
│   │   ├── controller/    AiController.java
│   │   └── service/       AiService.java
│   ├── admin/             后台管理
│   │   ├── controller/    AdminController.java
│   │   ├── service/       AdminService.java
│   │   ├── entity/        VisitorLog.java
│   │   ├── repository/    VisitorLogRepository
│   │   └── dto/           LoginRequest, LoginResponse, DashboardStats, ArticleDto
│   └── user/              用户/管理员
│       ├── entity/        User.java
│       ├── repository/    UserRepository
│       └── dto/           UserDto
├── common/                公共组件
│   ├── ApiResponse.java
│   ├── GlobalExceptionHandler.java
│   └── VisitorFilter.java
├── config/                配置
│   ├── SecurityConfig.java
│   ├── JwtAuthFilter.java
│   ├── JwtTokenProvider.java
│   ├── DataSeeder.java
│   ├── AdminUserBootstrapRunner.java
│   └── CorsConfig.java
└── meta/                  元数据控制器(单独)
    ├── MetaController.java
    └── MetaService.java
```

### 迁移清单
- [x] blog: ArticleController, ArticleService, Article, Category, Tag + repos
- [x] guestbook: GuestbookController, GuestbookService, Guestbook + repo
- [x] comment: CommentController, CommentService, Comment + repo
- [x] music: MusicController, MusicService, NcmApiClient (新增)
- [x] ai: AiController
- [x] user: User, UserRepository
- [x] common: ApiResponse, GlobalExceptionHandler, VisitorFilter (新增)
- [x] config: SecurityConfig, JwtAuthFilter, JwtTokenProvider, DataSeeder, AdminUserBootstrapRunner

---

## 阶段二：后端新功能

### 2.1 访问日志系统
- **VisitorLog 实体**：id, ip, userAgent, referer, path, method, visitTime
- **VisitorFilter**：拦截所有 HTTP 请求，自动记录
- **VisitorService**：查询、统计（日/周/月访问量, Top IP, 热门路径）
- **AdminController 接口**：
  - `GET /api/admin/stats` — 仪表盘概览
  - `GET /api/admin/visitors?page=&size=&start=&end=` — 访问日志列表
  - `GET /api/admin/visitors/stats` — 访问统计

### 2.2 管理员登录
- **User 实体扩展**：role 字段 (USER/ADMIN)
- **JWT 优化**：Token 包含 role，管理员判断
- **AdminController 接口**：
  - `POST /api/admin/login` — 管理员登录
  - `POST /api/admin/logout` — 登出
  - `GET /api/admin/me` — 当前管理员信息

### 2.3 后台管理 API
- **文章管理**：
  - `POST /api/admin/articles` — 创建文章
  - `PUT /api/admin/articles/{id}` — 编辑文章
  - `DELETE /api/admin/articles/{id}` — 删除文章
- **留言管理**：
  - `GET /api/admin/guestbooks` — 留言列表
  - `DELETE /api/admin/guestbooks/{id}` — 删除留言
- **仪表盘**：
  - `GET /api/admin/dashboard` — 文章数/留言数/访问量/今日统计

---

## 阶段三：前端重构

### 3.1 API 层 (`frontend/src/api/`)
- `index.ts` — axios 实例、拦截器、Token 注入
- `blog.ts` — 文章列表/详情
- `music.ts` — 网易云音乐搜索/歌单/歌曲
- `guestbook.ts` — 留言提交/列表
- `admin.ts` — 登录/管理 API

### 3.2 登录系统
- `LoginPage.vue` — 登录页面（毛玻璃风格）
- `stores/auth.ts` — Pinia 认证 Store（Token 持久化）
- 路由守卫 — 后台页面需要 admin 权限

### 3.3 后台管理界面
- `admin/DashboardPage.vue` — 概览仪表盘
- `admin/ArticleManager.vue` — 文章 CRUD
- `admin/GuestbookManager.vue` — 留言管理
- `admin/VisitorLogPage.vue` — 访问日志

### 3.4 音乐播放器
- 搜索框 → 搜索结果列表
- 歌单展示 → 歌曲列表
- 播放控制：播放/暂停、进度条、音量、上一首/下一首
- 歌词同步显示
- 迷你播放器栏（底部固定）

### 3.5 博客文章优化
- Markdown 渲染 (markdown-it)
- 代码语法高亮 (highlight.js)
- 文章目录 (TOC)
- 阅读量统计

### 3.6 游戏调整
- 移除 FlappyBird（像素鸟）
- 添加舒尔特方格 (Schulte Grid)

---

## 阶段四：移动端适配

### 4.1 导航
- 小屏：汉堡菜单 (hamburger)，全屏抽屉式导航
- 大屏：保持现有顶部导航

### 4.2 响应式布局
- 所有页面适配 375px — 1920px
- 全屏 Section 在手机上仍全屏但文字缩小
- 游戏容器响应式

### 4.3 游戏触屏
- 贪吃蛇：滑动手势控制方向
- 2048：滑动手势合并数字
- 舒尔特方格：点击数字

---

## 阶段五：部署优化 (2C2G)

### 5.1 容器资源限制
| 服务 | 内存限制 | CPU |
|------|---------|-----|
| MySQL 8.0 | 384MB | 0.5 |
| Redis 7 | 64MB | 0.25 |
| ncm-api | 256MB | 0.25 |
| Backend JVM | 256MB (-Xmx256m) | 0.5 |
| Nginx | 64MB | 0.25 |
| **总计** | **~1GB** | **1.75** |

### 5.2 JVM 调优
- `-Xmx256m -Xms128m -XX:+UseG1GC -XX:MaxGCPauseMillis=200`
- `-XX:+UseContainerSupport` (自适应容器内存)

### 5.3 MySQL 优化
- `--performance-schema=OFF`
- `--skip-log-bin`
- `--innodb-buffer-pool-size=64M`

### 5.4 docker-compose.yml
- 从 `deploy/docker-compose.example.yml` 扩展
- 添加 mem_limit + cpus 限制
- 添加健康检查超时时间

---

## 新增文件总览

```
后端 (15+ 文件):
  module/admin/controller/AdminController.java
  module/admin/service/AdminService.java
  module/admin/entity/VisitorLog.java
  module/admin/repository/VisitorLogRepository.java
  module/admin/dto/LoginRequest.java
  module/admin/dto/LoginResponse.java
  module/admin/dto/DashboardStats.java
  module/music/service/MusicService.java
  module/music/service/NcmApiClient.java
  module/music/dto/SongDto.java
  module/music/dto/PlaylistDto.java
  module/music/dto/LyricDto.java
  module/visitor/VisitorFilter.java
  common/VisitorFilter.java (or module/visitor)
  module/user/dto/UserDto.java

前端 (12+ 文件):
  src/api/index.ts
  src/api/blog.ts
  src/api/music.ts
  src/api/guestbook.ts
  src/api/admin.ts
  src/stores/auth.ts
  src/pages/LoginPage.vue
  src/pages/admin/DashboardPage.vue
  src/pages/admin/ArticleManager.vue
  src/pages/admin/GuestbookManager.vue
  src/pages/admin/VisitorLogPage.vue
  src/components/MarkdownRenderer.vue
```

---

## 风险与注意事项

1. **后端模块化**：移动文件后所有 import 需更新，编译验证是关键节点
2. **2C2G 限制严格**：JVM 256MB 可能不够，需观察 GC 日志，必要时调整
3. **ncm-api 稳定性**：依赖第三方镜像 moefurina/ncm-api，需要健康检查和降级处理
4. **密码管理**：不提交 .env 文件，仅提交 .env.example 模板
5. **MusicController /api/music/** 通配符路由** 需要放在最后注册，避免拦截其他 API
