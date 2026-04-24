CREATE TABLE IF NOT EXISTS portfolio_work (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(160) NOT NULL,
  short_desc VARCHAR(600) NOT NULL,
  detail TEXT NOT NULL,
  tag VARCHAR(100) NOT NULL,
  link VARCHAR(700) NOT NULL,
  cover_url VARCHAR(700) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL
);

INSERT INTO portfolio_work
  (title, short_desc, detail, tag, link, cover_url, enabled, sort_order, created_at, updated_at)
SELECT *
FROM (
  SELECT
    '大语言模型微调与部署' AS title,
    '基于 GLM4 与 LoRA 技术的大模型微调实战项目，探索垂直领域大语言模型应用。' AS short_desc,
    '覆盖数据构建、训练与推理部署流程，可作为 LLM 工程化落地的实践参考。' AS detail,
    'LLM / GLM4' AS tag,
    '#works' AS link,
    '/avatar.webp' AS cover_url,
    TRUE AS enabled,
    10 AS sort_order,
    NOW(6) AS created_at,
    NOW(6) AS updated_at
  UNION ALL
  SELECT
    'Transformer 机器翻译',
    '基于底层 Transformer 架构从零构建的机器翻译模型，深入理解 Attention 机制。',
    '从编码器—解码器到多头注意力，适合巩固序列建模与并行训练要点。',
    'Deep Learning',
    '#works',
    '/avatar.webp',
    TRUE,
    20,
    NOW(6),
    NOW(6)
  UNION ALL
  SELECT
    'MyWebSide Blog',
    '个人专属数字花园，基于 Spring Boot 3 与 Vue 3 构建的全栈展示平台。',
    '博客、留言墙、工具栏与 OJ 一体化；代码开源，欢迎交流与共建。',
    'Full Stack',
    'https://github.com/weihanyinian/mynewwebside',
    '/avatar.webp',
    TRUE,
    30,
    NOW(6),
    NOW(6)
  UNION ALL
  SELECT
    '在线判题 OJ',
    '内置算法题库与 Judge0 沙箱，支持 C/C++/Java/Python，ACM 与力扣风格评测。',
    '登录后可提交代码、查看评测状态与历史提交记录。',
    'OJ / Sandbox',
    '/tools/oj',
    '/avatar.webp',
    TRUE,
    40,
    NOW(6),
    NOW(6)
) AS seed
WHERE NOT EXISTS (SELECT 1 FROM portfolio_work);
