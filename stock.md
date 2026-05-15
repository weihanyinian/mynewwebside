# 模拟炒股功能前后端配合实现文档

> 目标：完整还原支付宝（蚂蚁财富）模拟炒股体验，实现前后端紧密配合的虚拟交易系统。
> 初始资金：100万元虚拟金，支持A股/美股/港股，限价单自动撮合，实时持仓盈亏更新。

---

## 1. 整体架构与数据流

### 前后端职责划分
- **前端（Vue 3 + TS + Element Plus）**：
  - 负责 UI 交互、搜索、交易弹窗、ECharts 图表渲染
  - 轮询获取行情（30s 持仓页，60s K线页）
  - 调用下单/撤单接口，展示实时反馈
- **后端（Spring Boot）**：
  - 行情抓取与缓存（StockDataService + Caffeine）
  - 资金/持仓校验与更新（StockService）
  - 订单撮合引擎（OrderMatchingScheduler 定时任务）
  - 持久化：StockPortfolio / StockTrade / StockOrder 三个表

### 核心数据流
1. 用户打开页面 → `fetchPortfolio()` + `fetchHotStocks()` + `fetchOrders()`
2. 搜索股票 → `searchStocks(keyword)` → 展示结果 → 点击快速交易
3. 下单：
   - 市价单：直接调用 `/buy` 或 `/sell`（立即成交）
   - 限价单：调用 `/order` 创建 PENDING 订单
4. 后端定时任务（每 30s）扫描 PENDING 订单，匹配现价后成交 → 更新持仓、现金、生成 Trade 记录
5. 前端轮询刷新 portfolio/orders → 看到最新持仓与订单状态
6. 进入详情页 → `fetchKline` + `fetchIntraday` + `fetchCapitalFlow` 渲染图表

---

## 2. 后端核心接口契约（StockController）

所有接口统一前缀 `/api/stock`，使用 `Principal` 获取当前登录用户。

### 2.1 行情与工具接口
| 方法 | 路径 | 参数 | 返回 | 说明 |
|------|------|------|------|------|
| GET | `/quote` | `code` | Quote | 实时报价（price, changePct 等） |
| POST | `/quotes` | `List<String> codes` | List<Quote> | 批量刷新持仓 |
| GET | `/intraday` | `code` | IntradayData | 分时数据 |
| GET | `/kline` | `code, period=day/week/month` | KlineData | K线 |
| GET | `/capital-flow` | `code` | CapitalFlow | 仅A股资金流向 |
| GET | `/hot` | `market=cn/us/hk` | List<HotStock> | 热门榜 |
| GET | `/search` | `keyword` | List<SearchResult> | 腾讯+Yahoo 智能搜索 |

### 2.2 交易与订单接口
| 方法 | 路径 | Body | 返回 | 说明 |
|------|------|------|------|------|
| GET | `/portfolio` | - | PortfolioSummary | 现金 + 持仓列表 + 总盈亏 |
| GET | `/trades` | - | List<TradeHistoryDto> | 成交历史 |
| GET | `/leaderboard` | - | List<LeaderboardEntry> | 盈利排行 |
| POST | `/buy` | `{code, shares}` | TradeResultDto | **市价立即买入** |
| POST | `/sell` | `{code, shares}` | TradeResultDto | **市价立即卖出** |
| POST | `/order` | `{code, type, orderType, limitPrice, shares}` | OrderDto | 创建委托单（LIMIT/MARKET） |
| GET | `/orders` | - | List<OrderDto> | 用户所有委托 |
| DELETE | `/order/{id}` | - | OrderDto | 撤单（仅 PENDING/PARTIAL） |

**关键 DTO 示例**（Java Record）：
```java
// 订单请求
record OrderRequest(String code, String type, String orderType, BigDecimal limitPrice, int shares) {}

// 订单返回
record OrderDto(Long id, String code, String name, String type, String orderType,
                BigDecimal price, int shares, int filledShares, String status,
                Instant createdAt, Instant updatedAt) {}
```

### 2.3 撮合逻辑（OrderMatchingScheduler）
- `@Scheduled(fixedDelay = 30000)`
- 查询所有 `status = PENDING` 的订单
- 买入限价单：`现价 <= limitPrice` → 成交
- 卖出限价单：`现价 >= limitPrice` → 成交
- 成交后：
  1. 更新 `StockOrder` → FILLED / PARTIAL
  2. 调用 `StockService` 内部买入/卖出逻辑（扣现金、更新持仓、生成 Trade）
  3. 记录 `filledShares`

---

## 3. 前端核心调用流程（ToolStockPage.vue + api/stock.ts）

### 3.1 初始化与刷新
```ts
async function refreshData() {
  const [p, t, lb, ord] = await Promise.all([
    fetchPortfolio(),
    fetchTrades(),
    fetchLeaderboard(),
    fetchOrders()
  ])
  portfolio.value = p
  // ... 更新 UI
}
setInterval(refreshData, 30000)  // 持仓页 30s 刷新
```

### 3.2 下单交互示例
```ts
// 市价买入
async function doMarketBuy(code: string, shares: number) {
  const res = await buyStock(code, shares)
  ElMessage.success(`买入成功，实际成交价 ${res.price}`)
  await refreshData()
}

// 限价委托
async function doLimitOrder(code: string, type: 'BUY'|'SELL', price: number, shares: number) {
  const res = await placeOrder(code, type, 'LIMIT', price, shares)
  ElMessage.success('委托已提交，等待撮合')
  await refreshData()
}
```

### 3.3 详情页数据获取（StockDetailPage.vue）
```ts
const kline = await fetchKline(code, 'day')      // ECharts candlestick
const intra = await fetchIntraday(code)          // 分时折线
const flow  = await fetchCapitalFlow(code)       // 饼图（仅A股）
```

---

## 4. 订单完整生命周期时序图（文字版）

```
用户点击「买入」按钮
    ↓
前端校验 shares > 0 && cash 足够
    ↓
POST /api/stock/order  {type: 'BUY', orderType: 'LIMIT', limitPrice: 100.5, shares: 100}
    ↓
后端创建 StockOrder (status=PENDING)
    ↓
返回 OrderDto 给前端（展示在「委托」Tab）
    ↓
OrderMatchingScheduler 每30s执行
    ↓
查询最新 Quote.price
    ↓
if (BUY && price <= limitPrice) → 成交
    ↓
@Transactional 更新：
  - StockOrder.status = FILLED
  - StockPortfolio 持仓 & 均价
  - StockTrade 新增 BUY 记录
  - Cash 扣减
    ↓
前端下次 refreshData() 看到：
  - 持仓增加
  - 订单状态变为 FILLED
  - 交易记录新增一条
```

---

## 5. 实时更新与性能优化

- **行情缓存**：StockDataService 使用 Caffeine，TTL 5s（A股）/ 15s（美股）
- **批量接口**：`/quotes` 支持持仓多股票一次刷新，减少请求数
- **前端防抖**：搜索框 300ms debounce
- **错误处理**：
  - 后端统一 `BusinessException` + `GlobalExceptionHandler`
  - 前端 `ElMessage.error` + 网络错误重试提示
- **并发控制**：买入/卖出使用 `@Transactional` + 乐观锁（版本号或直接查最新现金）

---

## 6. 边界情况与异常处理

1. **资金不足**：后端抛 `BusinessException(400, "可用资金不足")`，前端弹窗提示
2. **持仓不足卖出**：同上
3. **非交易时间**：目前不限制（模拟环境），后续可加交易日判断
4. **价格为0**：`fetchQuote` 返回 0 时前端显示 `--`，禁止交易
5. **撤单时机**：仅允许 status = PENDING / PARTIAL 的订单
6. **多用户排行**：Leaderboard 按所有 Trade.profitLoss 汇总（非实时，可后续优化为快照表）

---

## 7. 部署与监控要点

- **环境变量**：Yahoo Finance 调用频率限制（约60次/小时），生产环境建议加代理或付费 API
- **数据库索引**：`stock_orders(user_id, status)`、`stock_trades(user_id, traded_at)`
- **日志**：撮合任务开启 DEBUG 日志，记录成交详情
- **监控指标**：订单成交率、接口平均耗时、Caffeine 命中率
- **安全**：所有交易接口必须携带 JWT（Principal），禁止未登录调用

---

## 8. 后续演进方向（Phase 3+）

- WebSocket 推送行情与订单状态（替代轮询）
- 增加佣金、印花税、滑点模拟
- 技术指标叠加（MA、MACD、KDJ）
- 自选股云端同步
- 模拟打新、融资融券
- 移动端 H5 优化 + 指纹/手势交易确认

---

**文档版本**：v2.0（前后端配合实现版）  
**更新日期**：2026-05-15  
**维护者**：Grok + 用户  
**参考代码**：`StockController.java`、`StockService.java`、`ToolStockPage.vue`、`api/stock.ts`

本文档可直接作为开发手册或 PR 说明使用。