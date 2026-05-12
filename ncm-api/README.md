# 网易云增强 API（本仓库内嵌）

基于 npm 包 [@neteasecloudmusicapienhanced/api](https://www.npmjs.com/package/@neteasecloudmusicapienhanced/api)，与 Spring Boot `NeteaseBinaryifyClient` 使用的路由兼容（如 `/playlist/track/all`、`/song/url`、`/lyric`、`/cloudsearch`、`/login/cellphone`）。

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

## 配置

可选：复制 `.env.example` 为 `.env`，调整解灰、FLAC 等（详见包文档与镜像说明）。

## 与后端对接

`backend/src/main/resources/application.yml`（由 `application.example.yml` 复制）中设置：

- `netease.proxy.base-url: http://127.0.0.1:3000`
- `ncm.base-url: http://127.0.0.1:3000`

再启动 Spring Boot 与 `frontend` 的 `npm run dev`。

## 替代方式

也可用 Docker：`deploy/docker-compose.ncm-only.example.yml`（镜像 `moefurina/ncm-api`）。
