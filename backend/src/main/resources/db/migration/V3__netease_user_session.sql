-- 网易云第三方 API 登录态（Cookie 密文存储，与站点 JWT 用户绑定）

CREATE TABLE IF NOT EXISTS netease_user_session (
  username VARCHAR(64) NOT NULL PRIMARY KEY,
  cookie_cipher TEXT NOT NULL,
  netease_uid BIGINT,
  netease_nickname VARCHAR(128),
  updated_at DATETIME(6) NOT NULL
);
