-- Baseline schema for MySQL 8+ (JPA + MyBatis 共用表). 已有数据的库请配合 spring.flyway.baseline-on-migrate=true 跳过本脚本执行。
-- 新库将完整执行本迁移；列类型与 Hibernate 6 + 默认命名策略对齐。

CREATE TABLE IF NOT EXISTS category (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(64) NOT NULL UNIQUE,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL
);

CREATE TABLE IF NOT EXISTS tag (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(64) NOT NULL UNIQUE,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL
);

CREATE TABLE IF NOT EXISTS article (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  summary VARCHAR(400) NOT NULL,
  content_md LONGTEXT NOT NULL,
  cover_url VARCHAR(500),
  status VARCHAR(20) NOT NULL,
  category_id BIGINT,
  views BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  published_at DATETIME(6),
  CONSTRAINT fk_article_category FOREIGN KEY (category_id) REFERENCES category (id)
);

CREATE TABLE IF NOT EXISTS article_tag (
  article_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  PRIMARY KEY (article_id, tag_id),
  CONSTRAINT fk_article_tag_article FOREIGN KEY (article_id) REFERENCES article (id),
  CONSTRAINT fk_article_tag_tag FOREIGN KEY (tag_id) REFERENCES tag (id)
);

CREATE TABLE IF NOT EXISTS `comment` (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  article_id BIGINT NOT NULL,
  author VARCHAR(50) NOT NULL,
  content VARCHAR(1000) NOT NULL,
  create_time DATETIME(6) NOT NULL,
  CONSTRAINT fk_comment_article FOREIGN KEY (article_id) REFERENCES article (id)
);

CREATE TABLE IF NOT EXISTS wall_message (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  nickname VARCHAR(64) NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(20) NOT NULL,
  admin_reply VARCHAR(1000),
  created_at DATETIME(6) NOT NULL,
  reviewed_at DATETIME(6)
);

CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  nickname VARCHAR(64) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS oj_problem (
  id VARCHAR(64) NOT NULL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  difficulty VARCHAR(32) NOT NULL,
  judge_mode VARCHAR(32) NOT NULL,
  description LONGTEXT NOT NULL,
  input_desc TEXT NOT NULL,
  output_desc TEXT NOT NULL,
  time_limit_sec INT NOT NULL,
  memory_limit_mb INT NOT NULL,
  supported_langs_json LONGTEXT NOT NULL,
  samples_json LONGTEXT NOT NULL,
  test_cases_json LONGTEXT NOT NULL,
  reference_solution_json LONGTEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS oj_submission (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  username VARCHAR(64) NOT NULL,
  problem_id VARCHAR(64) NOT NULL,
  language VARCHAR(32) NOT NULL,
  source_code LONGTEXT NOT NULL,
  submitted BOOLEAN NOT NULL,
  verdict VARCHAR(32) NOT NULL,
  message TEXT,
  time_seconds DOUBLE,
  memory_kb INT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS friend_link (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(120) NOT NULL,
  url VARCHAR(600) NOT NULL,
  description VARCHAR(500),
  avatar_url VARCHAR(600),
  sort_order INT NOT NULL,
  created_at DATETIME(6) NOT NULL
);

CREATE TABLE IF NOT EXISTS code_snippet (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  language VARCHAR(64) NOT NULL,
  content LONGTEXT NOT NULL,
  updated_at DATETIME(6) NOT NULL
);
