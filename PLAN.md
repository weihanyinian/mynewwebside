# 网站优化 & 功能扩展计划

> 基于代码审查和开源项目调研 | 2026-05-12 更新

---

## 一、Phase 1 — 已完成

- [x] 模拟炒股基础模块 (A股, 新浪实时行情, 买入/卖出, 持仓, 排行榜)
- [x] 文章阅读进度条 + TOC 目录
- [x] 相关文章推荐
- [x] prefers-reduced-motion 全局支持
- [x] 音乐模块 NcmService 重写 (RestTemplate → RestClient)
- [x] MusicController Caffeine Cache
- [x] AiCompanionService RestClient 单例化

---

## 二、Phase 2 — 炒股模块增强（当前）

### 2.1 美股 + 港股支持

**数据源:**
- **美股**: Yahoo Finance unofficial API `query2.finance.yahoo.com/v8/finance/chart/{symbol}`
  - 无需 API Key, 15分钟延迟, 约60次/小时
  - 支持 OHLCV (开高低收量) 历史数据
- **港股**: 同上 Yahoo Finance, 后缀 `.HK` (如 `0700.HK`)
- **A股**: 保持新浪/腾讯接口

**Symbol 格式:**
- A股: `sh600519`, `sz000001`
- 美股: `AAPL`, `TSLA`, `MSFT`
- 港股: `0700.HK`, `9988.HK`

### 2.2 分时图 + K线图 (实时更新)

**图表库:** ECharts (已随 Element Plus 引入)

**分时图 (Time-sharing):**
- A股: 腾讯 `ifzq.gtimg.cn/appstock/app/minute/query` (分钟级实时)
- 美股/港股: Yahoo Finance `interval=5m` (5分钟K线代理分时)
- 展示: 价格线 + 均价线 + 成交量柱
- 实时更新: 每10秒轮询最新数据

**K线图:**
- 日K: Yahoo Finance `range=3mo&interval=1d`
- 周K: Yahoo Finance `range=6mo&interval=1wk`
- 月K: Yahoo Finance `range=2y&interval=1mo`
- 蜡烛图 (candlestick) + 成交量柱状图
- MA5/MA10/MA20 均线叠加
- Tab切换日K/周K/月K

### 2.3 热门股票模块

**数据源:**
- A股热门: 东方财富 `push2.eastmoney.com/api/qt/clist/get` (涨幅榜/跌幅榜)
- 美股热门: Yahoo Finance trending/suggestions

**前端展示:**
- 炒股主页顶部: 热门股票横向滚动卡片 (代码/名称/现价/涨跌幅)
- 涨幅榜: 当日涨幅前10
- 跌幅榜: 当日跌幅前10
- 点击跳转股票详情页
- 颜色: 红涨绿跌 (A股惯例) / 绿涨红跌 (美股惯例可切换)

### 2.4 委托订单系统

### 2.3 委托订单系统

**新增数据库表:**
```sql
CREATE TABLE stock_orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  stock_code VARCHAR(20) NOT NULL,
  stock_name VARCHAR(50),
  type ENUM('BUY','SELL') NOT NULL,
  order_type ENUM('LIMIT','MARKET') NOT NULL DEFAULT 'LIMIT',
  price DECIMAL(10,3) NOT NULL,
  shares INT NOT NULL,
  filled_shares INT DEFAULT 0,
  status ENUM('PENDING','PARTIAL','FILLED','CANCELLED') DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**功能:**
- 限价单: 指定价格买入/卖出，达到条件自动成交
- 市价单: 以当前市价立即成交
- 撤单: 取消未成交/部分成交的委托单
- 委托列表: 查看所有订单状态

**撮合逻辑:**
- 轮询检查: 每30秒扫描未成交订单
- 买入限价单: 现价 <= 委托价时成交
- 卖出限价单: 现价 >= 委托价时成交
- 参考项目: `ZaTribune/assets-matching-engine` (Spring Boot 3.4 + PriorityBlockingQueue)

### 2.4 资金流向（仅A股）

**数据源:** 东方财富 API
- 主力净流入: `https://push2.eastmoney.com/api/qt/stock/get?secid=1.600519&fields=f62,f64,f66,f72,f78,f184,f66`
- 返回大单/中单/小单/散户资金流向

**前端展示:**
- ECharts 饼图: 主力流入 vs 散户流入
- 仅A股支持，美股港股显示"暂无数据"
- 在股票详情页展示

### 2.5 股票详情页

**路由:** `/tools/stock/:code`

**页面内容:**
- 实时报价 (价格/涨跌幅/成交量)
- K线图 (日K/周K/月K tab切换)
- 资金流向饼图 (A股)
- 快速买入/卖出入口
- 返回模拟炒股主页

### 2.6 实时更新

- 持仓页: 30秒自动刷新行情
- 详情页K线: 60秒轮询最新数据
- 委托单状态: 30秒检查成交

---

## 三、Phase 3 — 博客发现层

- [ ] RSS + Sitemap
- [ ] SEO Meta (Open Graph / Twitter Card)
- [ ] Microsoft Clarity 接入
- [ ] 文章点赞/收藏

---

## 四、Phase 4 — 互动增强

- [ ] 全局搜索 (Ctrl+K)
- [ ] 评论嵌套回复
- [ ] Newsletter (Buttondown)
- [ ] E2E 测试

---

## 五、技术债务

| 优先级 | 项目 |
|--------|------|
| 高 | `api.js` + `user.js` 旧版 → TypeScript 迁移 |
| 高 | `Login.vue` 独立入口 → 整合到 `pages/auth/LoginPage.vue` |
| 中 | 全局错误边界 (Vue onErrorCaptured) |
| 中 | `doudizhu/main.js` Vue 2 → Vue 3 |

---

## 六、参考开源项目

| 项目 | 用途 |
|------|------|
| `chutrunganh/Stock-Trading-Platform` | 完整模拟交易系统 (Node+React) |
| `ZaTribune/assets-matching-engine` | Spring Boot 限价单撮合引擎 |
| `liihuu/KLineChart` | K线图组件 (MA/BOLL/MACD/KDJ) |
| Apache ECharts | K线 + 饼图 (项目已引入) |
| Yahoo Finance v8 API | 美股/港股 OHLCV 数据 |
