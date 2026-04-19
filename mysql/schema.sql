CREATE DATABASE IF NOT EXISTS blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE blog;

CREATE TABLE IF NOT EXISTS category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL UNIQUE,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tag (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL UNIQUE,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS article (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  summary VARCHAR(400) NOT NULL,
  content_md LONGTEXT NOT NULL,
  cover_url VARCHAR(500) NULL,
  status VARCHAR(20) NOT NULL,
  category_id BIGINT NULL,
  views BIGINT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  published_at DATETIME(3) NULL,
  CONSTRAINT fk_article_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS article_tag (
  article_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  PRIMARY KEY (article_id, tag_id),
  CONSTRAINT fk_article_tag_article FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE,
  CONSTRAINT fk_article_tag_tag FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_article_status_published_at ON article(status, published_at);
CREATE INDEX idx_article_category_id ON article(category_id);
CREATE INDEX idx_article_updated_at ON article(updated_at);
CREATE INDEX idx_article_tag_tag_id ON article_tag(tag_id);
