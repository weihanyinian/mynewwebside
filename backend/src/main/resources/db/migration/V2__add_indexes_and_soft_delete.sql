-- V2: 性能索引 + 文章软删除 + wall_message 状态索引
-- 适用于 MySQL 8+，配合 ddl-auto: validate 使用

-- ① wall_message 表：status + id 复合索引（前台按状态+倒序查询）
CREATE INDEX idx_wall_status_id ON wall_message(status, id DESC);

-- ② article 表：添加 deleted_at 软删除字段
ALTER TABLE article ADD COLUMN deleted_at DATETIME(6) NULL;

-- ③ article 表：deleted_at 索引（用于过滤已删除记录）
CREATE INDEX idx_article_deleted_at ON article(deleted_at);

-- ④ article 表：标题+摘要全文索引（替代 LIKE '%keyword%' 全表扫描）
-- 注意：content_md (LONGTEXT) 不纳入全文索引以避免索引过大
ALTER TABLE article ADD FULLTEXT INDEX ft_article_title_summary (title, summary);
