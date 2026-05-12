-- 作品展示：两条简单示例（仅当不存在同名作品时插入，可安全重复迁移）
INSERT INTO portfolio_work (
  title,
  short_desc,
  detail,
  content_md,
  tag,
  link,
  demo_url,
  repo_url,
  tech_stack,
  cover_url,
  enabled,
  sort_order,
  created_at,
  updated_at
)
SELECT
  '留言墙与访客互动',
  '访客可留言、分页浏览；管理员在后台审阅。',
  '与全站账号体系打通，适合作为首页「轻社交」入口。',
  '# 留言墙与访客互动\n\n记录访客想说的话，与博客形成互补。',
  '互动 / Message',
  '#works',
  '/message',
  NULL,
  'Spring Boot 3, Vue 3, MySQL',
  '/avatar.webp',
  TRUE,
  15,
  NOW(6),
  NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM portfolio_work WHERE title = '留言墙与访客互动');

INSERT INTO portfolio_work (
  title,
  short_desc,
  detail,
  content_md,
  tag,
  link,
  demo_url,
  repo_url,
  tech_stack,
  cover_url,
  enabled,
  sort_order,
  created_at,
  updated_at
)
SELECT
  '在线工具合集',
  '反应力、番茄钟、舒尔特方格等轻量小工具，即开即用。',
  '纯前端与少量后端接口结合，适合日常摸鱼与效率小测。',
  '# 在线工具合集\n\n从 `/tools` 进入工具中心，按需跳转各子页。',
  '工具 / Tools',
  '#works',
  '/tools',
  NULL,
  'Vue 3, TypeScript, Spring Boot',
  '/avatar.webp',
  TRUE,
  25,
  NOW(6),
  NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM portfolio_work WHERE title = '在线工具合集');
