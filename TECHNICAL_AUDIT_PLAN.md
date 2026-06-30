# 维寒一念的小站 — 技术审查与优化路线图

> **审计日期**：2026-06-30  
> **审计人**：Senior Developer (高级开发工程师)  
> **项目版本**：2.0.0  
> **技术栈**：Spring Boot 3.3 + Vue 3 + MySQL/Redis + Docker

---

## 一、当前架构概览

```
mywebsite/
├── backend/                          # Spring Boot 3.3 (Java 17)
│   └── src/main/java/.../website/
│       ├── WebsiteApplication.java
│       ├── common/                   # ApiResponse, GlobalExceptionHandler
│       ├── config/                   # Security, JWT, DataSeeder
│       └── module/
│           ├── blog/                 # 文章/分类/标签 (含 ArticleController)
│           ├── guestbook/            # 留言板
│           ├── comment/              # 评论
│           ├── music/                # 网易云音乐 (proxy → ncm-api:3000)
│           ├── ai/                   # AI 伴聊 (OpenAI API)
│           ├── admin/                # 管理后台 (JWT 登录 + 仪表盘 + CRUD)
│           ├── user/                 # 用户实体 (ADMIN/USER)
│           └── visitor/              # 访问日志 (VisitorFilter + 自动记录)
├── frontend/                         # Vue 3 + Vite + Tailwind CSS
│   └── src/
│       ├── api/                      # axios 实例 + blog/music/guestbook/admin 模块
│       ├── stores/                   # Pinia: auth, theme
│       ├── components/               # GlassCard, MusicPlayer, MarkdownRenderer...
│       ├── pages/                    # 页面 + admin/*, games/*, tools/*
│       └── router/                   # 路由 + 导航守卫
├── deploy/                           # docker-compose.yml (2C2G 优化版)
├── docker/                           # backend/frontend Dockerfiles
└── .env                              # 环境变量 (不入 Git)
```

| 指标 | 数值 |
|------|------|
| 后端 Java 文件 | 39 |
| 前端 Vue/TS 文件 | 38 |
| 后端编译状态 | ✅ 通过 |
| 数据库 | H2(本地) / MySQL(Docker) |
| 缓存 | Redis (可选，Docker 环境) |
| 测试覆盖率 | **0%** |

---

## 二、发现的问题清单

### 🔴 严重 (Security & Data Safety)

| ID | 问题 | 位置 | 影响 |
|----|------|------|------|
| S1 | CORS 配置 `allowCredentials(true)` + `allowedOriginPatterns("*")` | `SecurityConfig.java:55-56` | Cookie/Token 可被任意域名携带 |
| S2 | JWT secret 有硬编码默认值 `change-me-in-production...` | `application.yml:44` | 不设环境变量则使用弱密钥 |
| S3 | `GlobalExceptionHandler` 直接返回 `e.getMessage()` | `GlobalExceptionHandler.java:16` | 生产环境泄露堆栈/内部信息 |
| S4 | 登录接口无频率限制 | `AdminController.java:40` | 可被暴力破解 |
| S5 | H2 Console 在 local profile 暴露 | `application-local.yml:9` | 本地调试窗暴露，生产误启用有风险 |

### 🟠 高优先级 (Architecture & Maintainability)

| ID | 问题 | 位置 | 建议 |
|----|------|------|------|
| H1 | **零测试** | 全局 | 无单元测试、无集成测试 |
| H2 | `spring.jpa.open-in-view: true` | `application.yml:13` | 每次请求持有 DB 连接，2C2G 下连接池耗尽风险 |
| H3 | AI/Music 模块缺少 Service 层 | `AiController.java` `MusicController.java` | 业务逻辑耦合在 Controller |
| H4 | 无 API 版本化 | 所有 `/api/*` | 后续 API 变更会破坏前端 |
| H5 | `HomePage.vue` 220+ 行单文件 | 前端 | 需拆分为子组件 |
| H6 | 无数据库迁移工具 | 后端 | H2→MySQL 结构不一致风险 |

### 🟡 中优先级 (Quality & Robustness)

| ID | 问题 | 位置 | 建议 |
|----|------|------|------|
| M1 | 无输入校验 DTO | `GuestbookController` | nickname 长度、content 长度应限制 |
| M2 | 缺少 404 / 500 错误页面 | 前端 | 用户看到空白或 JSON 裸输出 |
| M3 | API 请求无 loading skeleton | 全局前端 | 目前都是文字 "加载中..." |
| M4 | `VisitorFilter` 同步写 DB | `VisitorFilter.java` | 每次请求阻塞等待 DB 写入 |
| M5 | 日志全在 stdout，无持久化 | 后端 | 重启丢失，无法追踪历史问题 |
| M6 | 无 SEO meta 标签 | `index.html` | 搜索引擎无法索引 |

### 🟢 低优先级 (Optimization & Polish)

| ID | 问题 | 位置 | 建议 |
|----|------|------|------|
| L1 | 手动 `vendor` chunk split 缺少常用库 | `vite.config.ts:26` | pinia/vue-router 已拆，但 markdown-it/highlight.js 未拆 |
| L2 | 游戏组件未懒加载 | `GamePage.vue` | SnakeGame/Game2048 每次都加载 |
| L3 | 无 PWA 支持 | 前端 | 离线访问、推送通知 |
| L4 | 无图片懒加载 | `ArticlePage.vue` | 大图直接加载 |
| L5 | 视频文件未压缩/未使用 CDN | `SiteBackground.vue` | 2C2G 服务器带宽瓶颈 |

---

## 三、优化路线图

### Phase 1：安全加固 (Week 1, 优先级最高)

| # | 任务 | 详细 | 预计时间 |
|---|------|------|---------|
| 1.1 | **CORS 白名单** | `allowedOriginPatterns` 改为从配置读取，生产限制为具体域名 | 30min |
| 1.2 | **JWT secret 强制** | 生产 profile 下若未设 `JWT_SECRET` 则启动失败 | 30min |
| 1.3 | **Exception Handler 脱敏** | 区分业务异常和系统异常；生产不暴露 `e.getMessage()` | 1h |
| 1.4 | **登录频率限制** | 基于 IP + username 的 Redis 计数器，5次/分钟锁定 | 2h |
| 1.5 | **输入校验** | Guestbook/Login/Comment 所有用户输入添加 `@Valid` + Bean Validation | 1h |
| 1.6 | **关闭 H2 Console** (非本地) | 仅在 `local` profile 启用，docker profile 禁用 | 30min |

### Phase 2：代码质量提升 (Week 1-2)

| # | 任务 | 详细 | 预计时间 |
|---|------|------|---------|
| 2.1 | **添加单元测试** | 至少 Service 层 80% 覆盖，JUnit 5 + Mockito | 4h |
| 2.2 | **添加集成测试** | Controller 层 `@WebMvcTest` + `@DataJpaTest` | 3h |
| 2.3 | **AI/Music Service 抽取** | AiService, MusicService 独立，Controller 仅做路由 | 2h |
| 2.4 | **关闭 open-in-view** | 改为 `false`，所有 Service 方法显式 `@Transactional(readOnly=true)` | 2h |
| 2.5 | **拆分 HomePage** | 拆为 HomeHero, HomeAbout, HomeBlog, HomeGuestbook, HomeGames 子组件 | 2h |
| 2.6 | **API 版本化** | `/api/v1/*`，旧 `/api/*` 保留 1 个月后废弃 | 1h |

### Phase 3：数据库与运维 (Week 2)

| # | 任务 | 详细 | 预计时间 |
|---|------|------|---------|
| 3.1 | **引入 Flyway** | 管理 DDL 变更历史，H2/MySQL 统一建表 | 3h |
| 3.2 | **异步化 VisitorFilter** | 使用 `@Async` + 线程池异步写访问日志 | 1h |
| 3.3 | **日志持久化** | Logback 配置：INFO 级别写文件 + 按天滚动 + 30天保留 | 1h |
| 3.4 | **健康检查增强** | Actuator health 加入 DB/Redis 连通性检查 | 30min |
| 3.5 | **添加备份脚本** | MySQL dump 定时任务 + 保留最近 7 天 | 1h |

### Phase 4：前端体验优化 (Week 2-3)

| # | 任务 | 详细 | 预计时间 |
|---|------|------|---------|
| 4.1 | **404 页面** | 优雅的毛玻璃 404 页面，带动画 | 1h |
| 4.2 | **Error Boundary** | Vue `onErrorCaptured` 全局错误处理 | 1h |
| 4.3 | **Loading Skeleton** | 博客列表/文章详情骨架屏 | 2h |
| 4.4 | **SEO 标签** | Open Graph + Twitter Card meta（标题/描述/封面图） | 1h |
| 4.5 | **SPA 懒加载优化** | markdown-it + highlight.js 独立 chunk，游戏组件独立 chunk | 30min |
| 4.6 | **图片懒加载** | `loading="lazy"` + Intersection Observer 渐进加载 | 1h |

### Phase 5：CI/CD 与文档 (Week 3)

| # | 任务 | 详细 | 预计时间 |
|---|------|------|---------|
| 5.1 | **GitHub Actions CI** | PR 时自动 build + test | 2h |
| 5.2 | **API 文档** | SpringDoc OpenAPI (Swagger UI) | 1h |
| 5.3 | **README 完善** | 本地开发、部署、架构图、API 说明 | 2h |
| 5.4 | **Content Security Policy** | Nginx 添加 CSP header | 30min |
| 5.5 | **性能基准测试** | JMeter 简单压测，记录 QPS/P99 延迟 | 2h |

---

## 四、技术债务汇总

```
总技术债务评估：Medium-High

Security:     ████████░░  8/10  (CORS 漏洞、JWT 弱密钥)
Testability:  ██████████ 10/10  (零测试)
Code Quality: ██████░░░░  6/10  (Service 层缺失、Controller 臃肿)
Monitoring:   █████████░  9/10  (无 APM、无告警)
DevOps:       ██████░░░░  6/10  (无 CI/CD、无迁移工具)
UX:           ████░░░░░░  4/10  (无骨架屏、无 404、无 SEO)
```

---

## 五、里程碑建议

```
Week 1: 安全加固 + Service 层重构  ──────▶  v2.1.0
Week 2: 测试补全 + Flyway + 异步化   ──────▶  v2.2.0
Week 3: 前端体验 + CI/CD + 文档     ──────▶  v2.3.0
```

**建议执行顺序**：优先完成 Phase 1 (安全) 和 Phase 2 中的 Service 重构，再推进测试和运维优化。

---

## 六、团队协作建议

| 角色 | 职责 |
|------|------|
| 后端开发 | Phase 1 (安全) + Phase 2 (测试/重构) + Phase 3 (运维) |
| 前端开发 | Phase 2 (HomePage 拆分) + Phase 4 (体验优化) |
| DevOps | Phase 5 (CI/CD + 文档) — 可由后端兼任 |

**Code Review 检查清单**：
- [ ] 是否存在 `e.getMessage()` 泄露到响应？
- [ ] 所有用户输入是否经过 `@Valid` 校验？
- [ ] Service 方法是否标注 `@Transactional`？
- [ ] 新增 API 是否添加到 Security 白名单？
- [ ] 前端是否对 API 调用做了错误状态处理？

---

> 文档生成时间：2026-06-30  
> 下次审查建议：完成 Phase 2 后重新评估
