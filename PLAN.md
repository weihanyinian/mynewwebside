# 网站优化 & 功能扩展计划

> 基于代码审查和开源项目调研，2026-05-12

---

## 一、模拟炒股模块（新增）

### 数据源
- **A股实时行情**：直接调用新浪/腾讯 HTTP 接口，无需 API Key
  - 新浪: `http://hq.sinajs.cn/list=sh600519`（约3秒延迟）
  - 腾讯: `https://qt.gtimg.cn/q=sz000001`
- **美股/港股**：可后续通过 Python sidecar (AKShare/yfinance) 扩展

### 技术架构
```
Vue 3 前端                           Spring Boot 后端
├── StockSimPage.vue                 ├── StockController (/api/stock)
│   ├── 股票搜索 (防抖输入)          │   ├── GET /quote?code=sh600519
│   ├── K线图 (ECharts/轻量canvas)   │   ├── GET /search?keyword=茅台
│   ├── 持仓面板 (盈亏计算)          │   ├── POST /trade (买入/卖出)
│   ├── 交易表单 (买/卖/数量)        │   ├── GET /portfolio (我的持仓)
│   └── 排行榜 (收益率TOP20)         │   └── GET /history (交易记录)
└── 路由: /tools/stock               └── MySQL: portfolios, trades 表
```

### 核心功能
1. **虚拟资金** — 注册用户自动获得 100万 虚拟资金
2. **实时搜索** — 输入代码/名称搜索股票，展示实时价格
3. **买入/卖出** — 按实时价格交易，扣手续费 0.03%
4. **持仓管理** — 展示持仓列表、成本、现价、盈亏额/盈亏率
5. **交易记录** — 历史买卖明细
6. **排行榜** — 总收益率排名（激发竞争/回访）
7. **自选股** — 关注列表，快速查看

### 数据库表设计
```sql
CREATE TABLE stock_portfolios (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  stock_code VARCHAR(20) NOT NULL,
  stock_name VARCHAR(50),
  shares INT NOT NULL,
  avg_cost DECIMAL(10,3) NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE stock_trades (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  stock_code VARCHAR(20) NOT NULL,
  stock_name VARCHAR(50),
  type ENUM('BUY','SELL') NOT NULL,
  shares INT NOT NULL,
  price DECIMAL(10,3) NOT NULL,
  fee DECIMAL(10,3) DEFAULT 0,
  profit_loss DECIMAL(10,3) DEFAULT 0,
  traded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 风险控制
- 不涉及真实资金，纯虚拟
- 价格有约3分钟延迟（合规）
- 页面标明"模拟交易，仅供学习参考"
- 调用新浪/腾讯免费接口，不商用

---

## 二、博客发现层优化（高ROI）

### 2.1 RSS + Sitemap
- **后端**：`GET /api/public/rss.xml` + `GET /sitemap.xml`
- 自动生成最近20篇文章的 RSS feed
- 搜索引擎提交入口

### 2.2 SEO Meta 标签
- 使用 `@vueuse/head` 为文章页注入 Open Graph / Twitter Card
- 后端拦截器对 `/article/*` 路由预渲染基础 meta（兼容爬虫）
- 让社交分享显示标题、摘要、封面图

### 2.3 文章阅读进度 + 目录
- 文章页顶部固定进度条（参考 Medium）
- 从 Markdown 标题自动生成右侧浮动 TOC
- 纯前端，不影响后端

### 2.4 相关文章推荐
- 文章详情页底部展示「相关推荐」
- 按相同标签匹配，取最近3篇
- API: `GET /api/public/articles?tagId=X&size=3&excludeId=Y`

### 2.5 真实数据分析
- 接入 Microsoft Clarity（免费、一行 script、热力图+录屏）
- Google Search Console 验证
- 用真实数据替换 localStorage 的自制计数器

---

## 三、用户体验优化

### 3.1 全局搜索增强
- 目前搜索只支持博客文章
- 扩展为全局搜索：文章 + 作品 + 工具 + OJ题目
- 快捷键 `Ctrl+K` 唤起搜索弹窗

### 3.2 社交互动
- 文章点赞/收藏（已有 JWT，加两个字段即可）
- 评论支持回复嵌套（目前是扁平评论）
- 留言墙 Emoji 表情选择器

### 3.3 无障碍优化
- `prefers-reduced-motion` 媒体查询：关闭动画
- 键盘导航焦点样式
- 图片 alt 文本完善

### 3.4 Newsletter
- Buttondock/ConvertKit 免费额度（1000订阅者）
- 文章页底部嵌入式表单
- 新文章自动推送

---

## 四、技术债务清理

| 优先级 | 项目 | 说明 |
|--------|------|------|
| 高 | `api.js` + `user.js` 旧版重构 | 遗留的 JS + 独立 axios 实例，应迁移到 TS |
| 高 | `Login.vue` 独立入口整合 | 与 `pages/auth/LoginPage.vue` 重复，删除旧版 |
| 高 | 后端 NcmController 独立 SecurityFilterChain | 简化安全配置 |
| 中 | 全局错误边界 | Vue `onErrorCaptured` + 统一错误页面 |
| 中 | E2E 测试 | Cypress/Playwright 覆盖登录→发文→查看文章 |
| 低 | `doudizhu/main.js` 独立入口整合 | 独立 Vue 2 小应用，迁移到 Vue 3 |

---

## 五、实施顺序建议

```
Phase 1 (本次)           Phase 2 (下次)            Phase 3 (后续)
├── 模拟炒股模块          ├── RSS + Sitemap          ├── 全局搜索增强
├── 文章阅读进度 + TOC    ├── SEO Meta               ├── 评论嵌套回复
├── 相关文章推荐          ├── MS Clarity 接入         ├── Newsletter
└── reduced-motion        └── 文章点赞/收藏           └── E2E 测试
```

---

## 六、确认事项

请确认以下内容，确认后开始 Phase 1 实现：

1. 模拟炒股功能添加到工具栏 (`/tools/stock`)
2. 使用新浪/腾讯免费股票数据接口
3. 虚拟资金100万，纯模拟不涉及真实金钱
4. 文章阅读进度条 + TOC + 相关文章推荐
5. `prefers-reduced-motion` 全局支持
