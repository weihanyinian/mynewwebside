-- QQ 音乐第三方 API 登录态（Cookie 密文存储，与站点 JWT 用户绑定）

CREATE TABLE IF NOT EXISTS qq_user_session (
  username VARCHAR(64) NOT NULL PRIMARY KEY,
  cookie_cipher TEXT NOT NULL,
  qq_uin VARCHAR(32),
  qq_nickname VARCHAR(128),
  updated_at DATETIME(6) NOT NULL
);
