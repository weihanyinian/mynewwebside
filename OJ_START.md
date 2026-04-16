# 在线判题（OJ）模块启动与集成说明

本仓库将 OJ 作为个人站点的独立小功能：**前端 Vue3 + TS + Tailwind**，**后端并入现有 Spring Boot 工程**，判题使用 **Judge0 CE** 公共沙箱，题目数据为 **`backend/src/main/resources/oj/problems.json`**（无数据库表）。

## 一、前端启动

```bash
cd frontend
npm install
npm run dev
```

默认通过 `VITE_API_BASE_URL` 指向后端（见 `frontend/src/api/http.ts`），未设置时为 `http://localhost:8080`。

访问路径：**`/oj`**（题目列表），**`/oj/p/oj-1`**（示例题目详情）。

### 前端文件位置（与路由对应）

| 文件 | 路径 |
|------|------|
| OJ 根布局 | `frontend/src/pages/oj/OjView.vue` |
| 题目列表 | `frontend/src/pages/oj/OjProblemList.vue` |
| 题目详情 + 编辑器 | `frontend/src/pages/oj/OjProblemDetail.vue` |
| API 封装 | `frontend/src/api/oj.ts` |

顶栏入口：`SiteLayout.vue` 已增加「在线 OJ / OJ」链接；`/oj` 使用与博客类似的**加宽主栏**，底部为 Live2D 与音乐播放器预留了内边距。

## 二、后端启动

与博客后端相同，主启动类仍为 **`com.mywebside.blog.BlogBackendApplication`**（**不要**再启一个单独的 `main`）。`com.mywebside.blog.oj.OJApplication` 仅为包说明占位。

```bash
cd backend
mvn spring-boot:run
# 或
mvn -DskipTests package && java -jar target/blog-backend-0.1.0.jar
```

### OJ 相关接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/oj/problems` | 题目列表，可选 `q`、`difficulty` |
| GET | `/api/oj/problems/{id}` | 题目详情（**不含**隐藏 `testCases`） |
| POST | `/api/oj/judge` | 运行或提交判题（JSON 体见下文） |

**安全配置**：`SecurityConfig` 已对 **`/api/oj/**`** `permitAll`；跨域由 **`CorsConfig`** 统一读取 `app.cors.allowed-origins`。

### 判题请求体示例

```json
{
  "problemId": "oj-1",
  "language": "PYTHON",
  "sourceCode": "a,b=map(int,input().split())\nprint(a+b)",
  "stdin": null,
  "submit": true
}
```

- `submit: false`：单次运行；`stdin` 为空时使用题目**第一个样例**输入。
- `submit: true`：对 `problems.json` 中**全部隐藏用例**依次评测，返回 `AC` / `WA` / `CE` / `RE` / `TLE`。

### 配置项（`application.yml`）

```yaml
oj:
  judge0:
    base-url: https://ce.judge0.com
    poll-interval-ms: 400
    poll-max-rounds: 80
    rapid-api-key: ""
    rapid-api-host: ""
```

公共 CE 实例一般**无需密钥**；若你改用 RapidAPI 等托管端点，可填写 `rapid-api-key` / `rapid-api-host`，后端会在请求头中附加 `X-RapidAPI-*`。

## 三、Judge0 沙箱说明

- 官方公共 CE：`https://ce.judge0.com`（免费、有速率与稳定性限制，适合个人站演示）。
- 本实现使用语言 ID：**C=50，C++=54，Java=62，Python=71**（与 CE 默认版本一致）。
- Java 提交需保持 **`public class Main`**，与 Judge0 单文件约定一致。

若公共实例不可用或频繁 502，可考虑自建 Judge0 或更换 `oj.judge0.base-url`。

## 四、集成到现有个人站的步骤（摘要）

1. **后端**：已将 Java 源码放在 `com.mywebside.blog.oj` 包下；题目数据在 `src/main/resources/oj/problems.json`。合并时保留 `CorsConfig`、`SecurityConfig` 中 OJ 与 CORS 相关修改。
2. **前端**：路由在 `src/router/index.ts` 注册 `/oj` 子路由；依赖含 CodeMirror 6（见 `frontend/package.json`）。
3. **联调**：保证 `app.cors.allowed-origins` 包含前端源（如 `http://localhost:5173`）。
4. **生产**：为 `/api/oj/judge` 设置合理反向代理超时（建议 ≥ 120s），避免网关提前断开。

## 五、`pom.xml` 说明

未新增依赖：使用 Spring Boot 自带的 **Jackson**、**RestClient**、**Validation** 即可对接 Judge0 与校验请求体。

## 六、本地记录

- 编辑器草稿：`localStorage` 键 `oj-draft-{problemId}`。
- 提交历史：`localStorage` 键 `oj-submissions-v1`（最近约 80 条）。
