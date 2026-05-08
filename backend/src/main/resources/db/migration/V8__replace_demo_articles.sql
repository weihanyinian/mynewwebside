-- 替换旧的简短演示文章为 1 篇高质量技术长文

-- 先确保分类和标签存在
INSERT INTO category (name, created_at, updated_at)
SELECT '技术', NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '技术');

INSERT INTO tag (name, created_at, updated_at)
SELECT 'SpringBoot', NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM tag WHERE name = 'SpringBoot');

INSERT INTO tag (name, created_at, updated_at)
SELECT 'Vue3', NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM tag WHERE name = 'Vue3');

INSERT INTO tag (name, created_at, updated_at)
SELECT '全栈', NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM tag WHERE name = '全栈');

INSERT INTO tag (name, created_at, updated_at)
SELECT '架构设计', NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM tag WHERE name = '架构设计');

-- 删除旧的 3 篇演示文章（清理关联先行）
DELETE FROM article_tag WHERE article_id IN (
  SELECT id FROM article WHERE title IN (
    '写在春天：把生活过成可复用模块',
    'Vue 3 页面体验优化：从交互细节到信息密度',
    'Spring Boot 博客后端的可维护性实践'
  )
);
DELETE FROM article WHERE title IN (
  '写在春天：把生活过成可复用模块',
  'Vue 3 页面体验优化：从交互细节到信息密度',
  'Spring Boot 博客后端的可维护性实践'
);

-- 插入新的技术长文
INSERT INTO article (
  title, summary, content_md, cover_url, status, category_id, views, created_at, updated_at, published_at, deleted_at
)
SELECT
  'Spring Boot 3 + Vue 3 全栈博客实战：从架构设计到性能优化',
  '从零搭建个人技术博客的完整记录，涵盖 Spring Boot 3 后端分层架构、Vue 3 组件化前端、JWT 认证体系、音乐 API 代理集成、Docker 容器化部署及全链路性能优化实践。',
  CONCAT(
    '## 项目背景与架构选型\n\n',
    '作为一个全栈开发者，一直想拥有一个完全由自己掌控的个人技术博客——不只是用现成的静态站点生成器，而是从后端到前端、从数据库到部署，每个环节都亲手设计。这篇文章就是对整个过程的完整复盘。\n\n',
    '### 技术选型\n\n',
    '| 层级 | 技术 | 选型理由 |\n',
    '|------|------|----------|\n',
    '| 后端框架 | Spring Boot 3.3 | 生态成熟，自动配置丰富，JPA + Flyway 组合省心 |\n',
    '| 数据库 | MySQL 9.1 + Redis | MySQL 存储业务数据，Redis 缓存浏览量、会话 |\n',
    '| 前端框架 | Vue 3.5 + Vite | Composition API 开发体验好，Vite 构建快 |\n',
    '| UI 组件库 | Element Plus 2 | 丰富的中后台组件，对 Vue 3 支持完整 |\n',
    '| 状态管理 | Pinia | 官方推荐，TypeScript 支持好 |\n',
    '| 容器化 | Docker + Compose | 一键部署，环境一致性 |\n',
    '| 反向代理 | Nginx | 静态资源 + API 代理统一入口 |\n\n',
    '### 架构全景\n\n',
    '```\n',
    '┌──────────────────────────────────────────┐\n',
    '│             Nginx :88                    │\n',
    '│     静态文件 /api 反代到 :8080             │\n',
    '└─────────┬────────────────────────────────┘\n',
    '          │\n',
    '    ┌─────┴──────────┐\n',
    '    │  前端 SPA :5173  │      音乐 API :3000\n',
    '    │  Vue 3 + Vite   │    (NeteaseMusicApi)\n',
    '    └────────┬────────┘          │\n',
    '             │                   │\n',
    '    ┌────────┴───────────────────┴──┐\n',
    '    │       后端 :8080               │\n',
    '    │   Spring Boot 3 + MySQL       │\n',
    '    └────────────────────────────────┘\n',
    '```\n\n',
    '## 后端设计：分层架构与核心能力\n\n',
    '### 1. 分包策略\n\n',
    '后端采用按业务领域分包的策略，每个模块自包含 Controller、Service、Repository：\n\n',
    '```\n',
    'com.mywebside.blog\n',
    '├── controller/          # 公共接口（博客、留言、作品展示）\n',
    '├── config/              # Security、CORS、Cache 配置\n',
    '├── domain/              # JPA 实体（Article、Category、Tag）\n',
    '├── dto/                 # 数据传输对象\n',
    '├── repo/                # Spring Data JPA Repository\n',
    '├── service/             # 业务逻辑层\n',
    '│   └── views/           # 浏览量计数（DB / Redis 双模式）\n',
    '└── music/\n',
    '    ├── netease/         # 网易云音乐代理\n',
    '    │   ├── proxy/       # 代理到 Binaryify/NeteaseCloudMusicApi\n',
    '    │   └── config/      # 网易云开放平台 API\n',
    '    └── qq/              # QQ 音乐 API 代理\n',
    '```\n\n',
    '### 2. JWT 认证体系\n\n',
    '采用无状态 JWT Token 认证，配合 Spring Security Filter Chain：\n\n',
    '```java\n',
    '@Bean\n',
    'public SecurityFilterChain filterChain(HttpSecurity http) {\n',
    '    http\n',
    '        .csrf(AbstractHttpConfigurer::disable)\n',
    '        .sessionManagement(s -> s\n',
    '            .sessionCreationPolicy(STATELESS))\n',
    '        .authorizeHttpRequests(auth -> auth\n',
    '            .requestMatchers(\"/api/public/**\")\n',
    '            .permitAll()\n',
    '            .requestMatchers(\"/api/admin/**\")\n',
    '            .hasRole(\"ADMIN\")\n',
    '            .requestMatchers(\"/api/music/**\")\n',
    '            .authenticated()\n',
    '            .anyRequest().permitAll()\n',
    '        )\n',
    '        .addFilterBefore(jwtAuthFilter,\n',
    '            UsernamePasswordAuthenticationFilter.class);\n',
    '    return http.build();\n',
    '}\n',
    '```\n\n',
    '关键设计决策：\n',
    '- 公共接口 `/api/public/**` 完全不需认证，游客即可浏览博客\n',
    '- 管理接口 `/api/admin/**` 需要 ADMIN 角色\n',
    '- 音乐接口 `/api/music/**` 需要已登录\（绑定账号后对接第三方）\n',
    '- JWT Token 存储在 localStorage，请求时通过 `Authorization: Bearer <token>` 携带\n\n',
    '### 3. Flyway 数据库演进\n\n',
    '使用 Flyway 管理所有 DDL 变更，拒绝"手动改库"：\n\n',
    '```sql\n',
    '-- V1: 初始建表（article、category、tag、article_tag、user）\n',
    '-- V2: 添加索引和软删除字段\n',
    '-- V3: 留言墙表\n',
    '-- V4: 网易云用户会话持久化\n',
    '-- V5: 博客演示数据种子\n',
    '-- V6: 作品展示表\n',
    '-- V7: QQ 音乐用户会话\n',
    '```\n\n',
    '每个迁移文件都是可重复执行的（幂等设计），比如种子数据用 `WHERE NOT EXISTS` 守卫。\n\n',
    '### 4. 浏览量计数策略\n\n',
    '一个有趣的设计挑战：每次阅读都直接 UPDATE MySQL 在高并发下压力大。我的方案是双模式切换：\n\n',
    '```java\n',
    '@ConditionalOnProperty(\n',
    '  name = \"app.view-counter.redis\",\n',
    '  havingValue = \"true\")\n',
    '@Component\n',
    'public class RedisArticleViewCounter\n',
    '    implements ArticleViewCounter {\n',
    '    // Redis INCR + 定时批量刷新到 MySQL\n',
    '}\n',
    '\n',
    '@ConditionalOnMissingBean(\n',
    '  RedisArticleViewCounter.class)\n',
    '@Component\n',
    'public class DbSyncArticleViewCounter\n',
    '    implements ArticleViewCounter {\n',
    '    // 直接写入 MySQL（默认模式）\n',
    '}\n',
    '```\n\n',
    '- **默认模式**：直接 UPDATE article SET views = views + 1（单机博客完全够用）\n',
    '- **Redis 模式**：INCR 到 Redis，每 60 秒通过 Lua 脚本批量同步到 MySQL\n\n',
    '## 前端设计：组件化与状态管理\n\n',
    '### 1. 路由与布局架构\n\n',
    '```typescript\n',
    'const routes = [\n',
    '  // 首页 — 独立布局\n',
    '  { path: \"/\",\n',
    '    component: PortfolioPage },\n',
    '  // 博客列表\n',
    '  { path: \"/blog\",\n',
    '    component: BlogHome },\n',
    '  // 文章详情\n',
    '  { path: \"/article/:id\",\n',
    '    component: ArticlePage },\n',
    '  // 音乐中心（需要登录）\n',
    '  { path: \"/music\",\n',
    '    component: MusicCenterPage,\n',
    '    meta: { requiresAuth: true } },\n',
    '  // 摸鱼小游戏\n',
    '  { path: \"/moyu/*\",\n',
    '    children: [...] },\n',
    '  // 后台管理\n',
    '  { path: \"/admin/*\",\n',
    '    component: AdminLayout },\n',
    ']\n',
    '```\n\n',
    '三种布局模式：\n',
    '- **首页**：独立全屏设计，视频背景 + 玻璃拟态\n',
    '- **内容页**：SiteLayout 壳（顶部导航 + 底部 + 面包屑）\n',
    '- **后台**：AdminLayout（侧边栏 + 暗色主题固定）\n\n',
    '### 2. 状态管理设计（Pinia）\n\n',
    '```typescript\n',
    '// musicPlayer store — 全局播放状态\n',
    'useMusicPlayerStore: {\n',
    '  queue, currentIndex, resolvedUrl,\n',
    '  neteaseBound, qqBound,\n',
    '  playMode, volume,\n',
    '  initPlayer(), loadSong(),\n',
    '  playNext()\n',
    '}\n',
    '\n',
    '// theme store — 主题切换\n',
    'useThemeStore: {\n',
    '  isDarkMode, followSystem,\n',
    '  toggleTheme(),\n',
    '  syncDocumentTheme()\n',
    '}\n',
    '```\n\n',
    '### 3. 国际化（i18n）\n\n',
    '使用 Vue I18n Composition API，中英文双语支持：\n',
    '- 所有页面文案、导航、工具提示均已国际化\n',
    '- 语言偏好存储在 localStorage，切换即时生效\n',
    '- 导航栏"中/EN"按钮一键切换\n\n',
    '### 4. 首页玻璃拟态设计\n\n',
    '首页采用了多层视觉设计：\n',
    '- **背景视频**：作为视觉底层，降低透明度保留动态感\n',
    '- **霓虹光晕**：跟随鼠标的视差效果（CSS 变量 + translate3d）\n',
    '- **玻璃卡片**：`background: rgba(255,255,255,0.1)` + `backdrop-filter: blur(5px)`\n',
    '- **噪声纹理**：叠在背景上的 SVG 纹理，模拟纸质质感\n',
    '- **扫描线**：极低透明度的 repeating-linear-gradient，致敬老式显示器\n\n',
    '## 音乐模块：Netease API 代理架构\n\n',
    '### 整体架构\n\n',
    '```\n',
    '浏览器 ─→ Spring Boot :8080/api/music/*\n',
    '    ─→ NeteaseCloudMusicApi :3000\n',
    '    ─→ 网易云官方 API\n',
    '\n',
    '浏览器 ─→ Spring Boot :8080/api/music/*\n',
    '    ─→ QQMusicApi :3300\n',
    '    ─→ QQ 音乐 API\n',
    '```\n\n',
    '为什么要做一层代理而不是前端直接调？\n',
    '1. **Cookie 安全**：登录 Cookie 存储在服务端，不暴露给浏览器\n',
    '2. **跨域问题**：第三方 API 不设 CORS，浏览器直调会失败\n',
    '3. **统一鉴权**：前端只需本站 JWT，后端判断权限\n',
    '4. **日志与监控**：所有第三方调用经过后端，方便排查\n\n',
    '### NeteaseBinaryifyClient 设计\n\n',
    '```java\n',
    '@Component\n',
    'public class NeteaseBinaryifyClient {\n',
    '    private final RestTemplate rest;\n',
    '    private final NeteaseProxyProperties props;\n',
    '\n',
    '    public <T> T get(String path,\n',
    '        Map<String,String> params,\n',
    '        Class<T> type) {\n',
    '        String url = buildUrl(path, params);\n',
    '        for (int i = 0;\n',
    '             i <= props.getRetryCount();\n',
    '             i++) {\n',
    '            try {\n',
    '                return rest.getForObject(\n',
    '                    url, type);\n',
    '            } catch (Exception e) {\n',
    '                if (i == props.getRetryCount())\n',
    '                    throw e;\n',
    '                // 本地不可达时\n',
    '                // 回退到公网镜像\n',
    '            }\n',
    '        }\n',
    '    }\n',
    '}\n',
    '```\n\n',
    '### 双源热歌策略\n\n',
    '默认歌单每日轮换：奇数日优先网易云热歌，偶数日优先 QQ 热歌。一方失败时自动回退到另一方，都失败时回退到配置的固定歌单。这一设计兼顾了新鲜感和可靠性。\n\n',
    '## 性能优化实践\n\n',
    '### 前端\n',
    '- **代码分割**：Vite 自动按路由懒加载，Monaco Editor 独立 2.5MB chunk\n',
    '- **图片优化**：WebP 格式优先，`loading=\"lazy\"` 延迟加载\n',
    '- **CSS 优化**：Tailwind JIT 模式，只生成使用的样式\n',
    '- **缓存策略**：静态资源哈希命名，配合 Nginx `expires 1y`\n\n',
    '### 后端\n',
    '- **连接池**：HikariCP 管理 MySQL 连接\n',
    '- **索引**：文章表 `published_at`、`category_id` 均已建索引\n',
    '- **软删除**：文章不物理删除，`deleted_at` + 索引快速过滤\n',
    '- **API 聚合**：音乐中心首页通过 Promise.all 并行请求，缩短首屏\n\n',
    '## Docker 部署方案\n\n',
    '```yaml\n',
    '# deploy/docker-compose.example.yml 结构\n',
    'services:\n',
    '  mysql:\n',
    '    image: mysql:9.1\n',
    '  redis:\n',
    '    image: redis:7-alpine\n',
    '    # 可选，浏览量计数用\n',
    '  backend:\n',
    '    build: ../docker/backend\n',
    '    # Spring Boot JAR\n',
    '  frontend:\n',
    '    build: ../docker/frontend\n',
    '    # Nginx + Vue 构建产物\n',
    '  ncm-api:\n',
    '    image: moefurina/ncm-api\n',
    '    # NeteaseCloudMusicApi Enhanced\n',
    '  qq-api:\n',
    '    build: ../docker/qq-music-api\n',
    '    # QQMusicApi\n',
    '```\n\n',
    '使用 `docker compose up -d` 即可启动全部服务。支持通过 `.env` 文件配置各服务环境变量。\n\n',
    '## 总结与展望\n\n',
    '这个项目让我对全栈开发有了更系统的理解：\n\n',
    '1. **分层不是教条**：个人项目不需要过度设计，但 Controller-Service-Repository 三层让后续维护轻松很多\n',
    '2. **数据库迁移是底线**：Flyway 让任何环境（开发/测试/生产）的数据库状态可追溯、可复现\n',
    '3. **状态管理要克制**：不是所有状态都要进 Pinia——斗地主游戏全程用组件内 `reactive()` 管理，简单高效\n',
    '4. **代理层有价值**：音乐 API 的代理设计解决了安全（Cookie 不暴露）和跨域问题\n',
    '5. **好体验在细节**：自动跳过无版权歌曲、日夜间主题跟随系统、响应式到 320px——这些小事比大功能更重要\n\n',
    '后续计划：\n',
    '- 评论系统接入（目前留言墙已就位）\n',
    '- 文章全文搜索（Elasticsearch 或 MySQL 全文索引）\n',
    '- CI/CD Pipeline（GitHub Actions 自动构建 Docker 镜像）\n',
    '- 访问统计仪表盘\n\n',
    '*如果你也在搭建个人博客，希望这篇文章能给你一些参考。代码全部开源在 [GitHub](https://github.com/weihanyinian/website)。*'
  ),
  '/avatar.webp',
  'PUBLISHED',
  (SELECT id FROM category WHERE name = '技术' LIMIT 1),
  128,
  NOW(6),
  NOW(6),
  NOW(6),
  NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM article
  WHERE title = 'Spring Boot 3 + Vue 3 全栈博客实战：从架构设计到性能优化'
    AND deleted_at IS NULL
);

-- 文章与标签关联
INSERT INTO article_tag (article_id, tag_id)
SELECT a.id, t.id
FROM article a
JOIN tag t ON t.name = 'SpringBoot'
WHERE a.title = 'Spring Boot 3 + Vue 3 全栈博客实战：从架构设计到性能优化'
  AND NOT EXISTS (
    SELECT 1 FROM article_tag at2 WHERE at2.article_id = a.id AND at2.tag_id = t.id
  );

INSERT INTO article_tag (article_id, tag_id)
SELECT a.id, t.id
FROM article a
JOIN tag t ON t.name = 'Vue3'
WHERE a.title = 'Spring Boot 3 + Vue 3 全栈博客实战：从架构设计到性能优化'
  AND NOT EXISTS (
    SELECT 1 FROM article_tag at2 WHERE at2.article_id = a.id AND at2.tag_id = t.id
  );

INSERT INTO article_tag (article_id, tag_id)
SELECT a.id, t.id
FROM article a
JOIN tag t ON t.name = '全栈'
WHERE a.title = 'Spring Boot 3 + Vue 3 全栈博客实战：从架构设计到性能优化'
  AND NOT EXISTS (
    SELECT 1 FROM article_tag at2 WHERE at2.article_id = a.id AND at2.tag_id = t.id
  );

INSERT INTO article_tag (article_id, tag_id)
SELECT a.id, t.id
FROM article a
JOIN tag t ON t.name = '架构设计'
WHERE a.title = 'Spring Boot 3 + Vue 3 全栈博客实战：从架构设计到性能优化'
  AND NOT EXISTS (
    SELECT 1 FROM article_tag at2 WHERE at2.article_id = a.id AND at2.tag_id = t.id
  );
