-- MySQL 8+：用户与 OJ 表（博客 article / wall_message 等由 JPA 自动维护）
-- 若使用项目内 AuthOjSchemaRunner，可不必手动执行本脚本；本文件便于 DBA 审阅与离线建库。

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  nickname VARCHAR(64) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS oj_problem (
  id VARCHAR(64) PRIMARY KEY,
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

CREATE TABLE IF NOT EXISTS mind_map (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  data LONGTEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_mind_map_user (user_id)
);

CREATE TABLE IF NOT EXISTS oj_submission (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
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

-- 内置管理员请勿手写密码入库；请通过环境变量在首次启动时由应用创建，例如：
--   export BOOTSTRAP_ADMIN_PASSWORD='你的强密码'
--   并在 application.yml 中设置：app.bootstrap-admin-password: ${BOOTSTRAP_ADMIN_PASSWORD:}
-- 切勿将生产环境密码提交到 Git。
