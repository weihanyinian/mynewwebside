# 网易云 api-enhanced（本仓库内嵌）

上游项目：[NeteaseCloudMusicApiEnhanced/api-enhanced](https://github.com/NeteaseCloudMusicApiEnhanced/api-enhanced)（第三方网易云 Node API，含解灰、FLAC 等）。

本目录通过 npm 包 [@neteasecloudmusicapienhanced/api](https://www.npmjs.com/package/@neteasecloudmusicapienhanced/api) 启动与官方文档一致的 HTTP 路由（如 `/playlist/track/all`、`/song/url`、`/song/url/v1`、`/lyric`、`/cloudsearch`、`/login/cellphone`、`/login/qr/*`），供 Spring Boot 的 `netease.proxy.base-url` 与 `ncm.base-url` 使用。

## 启动

```bash
cd ncm-api
npm install
npm start
```

默认监听 **3000**。Windows PowerShell 改端口：

```powershell
$env:PORT=3001; npm start
```

## 配置（与 api-enhanced 文档一致）

复制 `.env.example` 为 `.env` 后按需调整，常用项：

| 变量 | 说明 |
|------|------|
| `ENABLE_GENERAL_UNBLOCK` | 全局解灰（推荐 `true`） |
| `ENABLE_FLAC` | 无损相关能力 |
| `SELECT_MAX_BR` | 是否选最高码率 |
| `FOLLOW_SOURCE_ORDER` | 音源匹配顺序 |
| `CORS_ALLOW_ORIGIN` | 跨域来源 |

详见 [在线文档](https://neteasecloudmusicapienhanced.js.org/) 与上游 README。

## 与后端对接

在 `backend/.../application.yml`（可由 `application.example.yml` 复制）中设置：

- `netease.proxy.base-url: http://127.0.0.1:3000`
- `ncm.base-url: http://127.0.0.1:3000`

再启动 Spring Boot 与前端 `npm run dev`。

## 替代方式

Docker：`deploy/docker-compose.ncm-only.example.yml`（镜像 `moefurina/ncm-api`，与 api-enhanced 发布同源）。
