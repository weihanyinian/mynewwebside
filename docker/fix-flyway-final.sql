-- =============================================
-- Flyway 彻底修复：清空历史表并 baseline 到最新版本 V6
-- 这样 Spring Boot 启动时不会再尝试执行已完成的 migration
-- =============================================

USE blog;

-- 1. 清空 flyway 历史表
TRUNCATE TABLE flyway_schema_history;

-- 2. 插入 baseline 记录，标记 V6 及之前的 migration 已完成
INSERT INTO flyway_schema_history (
    installed_rank,
    version,
    description,
    type,
    script,
    checksum,
    installed_by,
    installed_on,
    execution_time,
    success
) VALUES (
    1,
    '6',
    '<< Flyway Baseline >>',
    'BASELINE',
    '<< Flyway Baseline >>',
    NULL,
    'root',
    NOW(),
    0,
    1
);
