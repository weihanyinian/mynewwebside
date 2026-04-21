-- 在已能登录 MySQL 的前提下执行（示例：mysql -u root -p < scripts/init_mysql_blog.sql）
-- 若 JDBC URL 已带 createDatabaseIfNotExist=true 且用户有建库权限，可跳过本脚本
-- 需要「清空并重建」blog 库时取消下一行注释（会删除库内全部数据）：
-- DROP DATABASE IF EXISTS blog;

CREATE DATABASE IF NOT EXISTS blog
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 若使用独立业务账号（可选），取消下面注释并改密码：
-- CREATE USER IF NOT EXISTS 'blog'@'localhost' IDENTIFIED BY '你的密码';
-- GRANT ALL PRIVILEGES ON blog.* TO 'blog'@'localhost';
-- FLUSH PRIVILEGES;
