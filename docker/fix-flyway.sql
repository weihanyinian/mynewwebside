USE blog;

-- 删除旧的 flyway 历史表（如果存在）
DROP TABLE IF EXISTS flyway_schema_history;

-- 创建 flyway 历史表
CREATE TABLE flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INT NOT NULL,
    success TINYINT(1) NOT NULL,
    PRIMARY KEY (installed_rank)
);

-- 插入基线记录，标记 version 1 已执行
INSERT INTO flyway_schema_history (
    installed_rank, version, description, type, script,
    checksum, installed_by, execution_time, success
) VALUES (
    1, '1', 'existing schema from ddl-auto era', 'BASELINE', 'baseline',
    NULL, 'root', 0, 1
);
