-- 若博客文章为空，则写入 3 篇示例文章，便于首页“阅读博客”落地展示

INSERT INTO category (name, created_at, updated_at)
SELECT '随笔', NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '随笔');

INSERT INTO category (name, created_at, updated_at)
SELECT '技术', NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '技术');

INSERT INTO tag (name, created_at, updated_at)
SELECT '生活', NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM tag WHERE name = '生活');

INSERT INTO tag (name, created_at, updated_at)
SELECT 'Vue3', NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM tag WHERE name = 'Vue3');

INSERT INTO tag (name, created_at, updated_at)
SELECT 'SpringBoot', NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM tag WHERE name = 'SpringBoot');

INSERT INTO article (
  title, summary, content_md, cover_url, status, category_id, views, created_at, updated_at, published_at, deleted_at
)
SELECT
  seed.title,
  seed.summary,
  seed.content_md,
  seed.cover_url,
  'PUBLISHED',
  seed.category_id,
  0,
  NOW(6),
  NOW(6),
  NOW(6),
  NULL
FROM (
  SELECT
    '写在春天：把生活过成可复用模块' AS title,
    '记录近期关于节奏、学习与创作的小变化，让日常也有一点工程化的秩序。' AS summary,
    '## 写在前面\n\n最近在想，生活其实也像系统设计：\n\n- 目标要可拆解\n- 节奏要可持续\n- 反馈要可观测\n\n## 一点实践\n\n我把每天固定成三个时间块：学习、输出、复盘。哪怕每块只有 30 分钟，也比“等有空再说”更有效。\n\n## 结语\n\n愿我们都能在平凡日子里，持续构建自己的版本迭代。' AS content_md,
    '/avatar.webp' AS cover_url,
    (SELECT id FROM category WHERE name = '随笔' LIMIT 1) AS category_id
  UNION ALL
  SELECT
    'Vue 3 页面体验优化：从交互细节到信息密度',
    '复盘个人站前端重构过程，重点分享组件层级、状态拆分与视觉一致性处理。',
    '## 背景\n\n随着页面增多，最容易出现的问题不是“功能不能跑”，而是“体验不统一”。\n\n## 我做了什么\n\n1. 统一导航与按钮语义\n2. 把重复样式抽成共享 token\n3. 首页、博客、工具页对齐交互反馈\n\n## 收获\n\n当样式系统和组件边界清晰后，后续加新页面会明显更轻松。' AS content_md,
    '/avatar.webp' AS cover_url,
    (SELECT id FROM category WHERE name = '技术' LIMIT 1) AS category_id
  UNION ALL
  SELECT
    'Spring Boot 博客后端的可维护性实践',
    '围绕接口分层、DTO 设计与后台管理流程，聊聊个人项目里最实用的后端工程习惯。',
    '## 为什么先做结构\n\n个人项目后期最怕“能跑但不敢改”。\n\n## 我的做法\n\n- Controller 保持薄层\n- Service 只放业务规则\n- DTO 明确输入输出边界\n- Flyway 管理数据库演进\n\n## 结果\n\n在新增作品管理、评论管理时，几乎可以沿用现有模式快速扩展。' AS content_md,
    '/avatar.webp' AS cover_url,
    (SELECT id FROM category WHERE name = '技术' LIMIT 1) AS category_id
) AS seed
WHERE NOT EXISTS (SELECT 1 FROM article WHERE deleted_at IS NULL);

-- 文章与标签关联（只在首次种子时插入）
INSERT INTO article_tag (article_id, tag_id)
SELECT a.id, t.id
FROM article a
JOIN tag t ON t.name = '生活'
WHERE a.title = '写在春天：把生活过成可复用模块'
  AND NOT EXISTS (
    SELECT 1 FROM article_tag at2 WHERE at2.article_id = a.id AND at2.tag_id = t.id
  );

INSERT INTO article_tag (article_id, tag_id)
SELECT a.id, t.id
FROM article a
JOIN tag t ON t.name = 'Vue3'
WHERE a.title = 'Vue 3 页面体验优化：从交互细节到信息密度'
  AND NOT EXISTS (
    SELECT 1 FROM article_tag at2 WHERE at2.article_id = a.id AND at2.tag_id = t.id
  );

INSERT INTO article_tag (article_id, tag_id)
SELECT a.id, t.id
FROM article a
JOIN tag t ON t.name = 'SpringBoot'
WHERE a.title = 'Spring Boot 博客后端的可维护性实践'
  AND NOT EXISTS (
    SELECT 1 FROM article_tag at2 WHERE at2.article_id = a.id AND at2.tag_id = t.id
  );
