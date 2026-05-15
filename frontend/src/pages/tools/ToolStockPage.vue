<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  fetchQuote, searchStocks, fetchPortfolio, fetchTrades, fetchLeaderboard,
  buyStock, sellStock, fetchHotStocks, placeOrder, cancelOrder, fetchOrders,
  type StockQuote, type StockSearchResult, type PortfolioSummary,
  type TradeHistoryItem, type LeaderboardEntry, type HotStockItem, type OrderItem,
} from '../../api/stock'

type Market = 'cn' | 'us' | 'hk'

const POPULAR: Record<Market, { code: string; name: string }[]> = {
  cn: [
    { code: 'sh600519', name: '贵州茅台' }, { code: 'sz000001', name: '平安银行' },
    { code: 'sh600036', name: '招商银行' }, { code: 'sz300750', name: '宁德时代' },
    { code: 'sh601318', name: '中国平安' }, { code: 'sz002594', name: '比亚迪' },
    { code: 'sh600900', name: '长江电力' }, { code: 'sz000858', name: '五粮液' },
  ],
  us: [
    { code: 'AAPL', name: 'Apple' }, { code: 'TSLA', name: 'Tesla' },
    { code: 'MSFT', name: 'Microsoft' }, { code: 'NVDA', name: 'NVIDIA' },
    { code: 'GOOGL', name: 'Alphabet' }, { code: 'AMZN', name: 'Amazon' },
    { code: 'META', name: 'Meta' }, { code: 'AMD', name: 'AMD' },
  ],
  hk: [
    { code: '0700.HK', name: '腾讯控股' }, { code: '9988.HK', name: '阿里巴巴' },
    { code: '0941.HK', name: '中国移动' }, { code: '3690.HK', name: '美团' },
    { code: '2318.HK', name: '中国平安' }, { code: '0388.HK', name: '港交所' },
    { code: '1810.HK', name: '小米集团' }, { code: '2269.HK', name: '药明生物' },
  ],
}

const MARKET_LABELS: Record<Market, string> = { cn: 'A股', us: '美股', hk: '港股' }

const market = ref<Market>('cn')
const hotStocks = ref<HotStockItem[]>([])

import { watch } from 'vue'
watch(market, async (m) => {
  try { hotStocks.value = await fetchHotStocks(m) } catch { hotStocks.value = [] }
}, { immediate: true })
const quoteCache = ref<Map<string, StockQuote>>(new Map())

const searchKeyword = ref('')
const searchResults = ref<StockSearchResult[]>([])
const searchLoading = ref(false)
let searchTimer: ReturnType<typeof setTimeout> | null = null

const portfolio = ref<PortfolioSummary | null>(null)
const trades = ref<TradeHistoryItem[]>([])
const leaderboard = ref<LeaderboardEntry[]>([])
// activeTab declared below with orders support

const tradeCode = ref('')
const tradeName = ref('')
const tradePrice = ref<number | null>(null)
const tradeShares = ref(100)
const tradeDialog = ref(false)
const tradeMode = ref<'BUY' | 'SELL'>('BUY')
const orderType = ref<'LIMIT' | 'MARKET'>('LIMIT')
const limitPrice = ref<number | null>(null)

const orders = ref<OrderItem[]>([])
const activeTab = ref<'portfolio' | 'trades' | 'leaderboard' | 'orders'>('portfolio')

const loading = ref(true)

const INITIAL_CASH = 1_000_000

function fmt(n: number | null | undefined, decimals = 2): string {
  if (n == null) return '--'
  return Number(n).toFixed(decimals)
}

function pnlClass(v: number): string {
  if (v > 0) return 'pnl-up'
  if (v < 0) return 'pnl-down'
  return 'pnl-zero'
}

async function refreshData() {
  try {
    const [p, t, lb] = await Promise.all([
      fetchPortfolio().catch(() => null),
      fetchTrades().catch(() => [] as TradeHistoryItem[]),
      fetchLeaderboard().catch(() => [] as LeaderboardEntry[]),
    ])
    portfolio.value = p
    trades.value = t
    leaderboard.value = lb
    orders.value = await fetchOrders().catch(() => [] as OrderItem[])
  } catch { /* ignore */ }
  loading.value = false
}

function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  const kw = searchKeyword.value.trim()
  if (!kw) { searchResults.value = []; return }
  searchTimer = setTimeout(async () => {
    searchLoading.value = true
    try {
      searchResults.value = await searchStocks(kw)
    } catch { searchResults.value = [] }
    finally { searchLoading.value = false }
  }, 300)
}

function selectStock(item: StockSearchResult) {
  searchKeyword.value = `${item.name} (${item.code})`
  searchResults.value = []
  tradeCode.value = item.fullCode
  tradeName.value = item.name
  fetchCurrentPrice(item.fullCode)
}

async function fetchCurrentPrice(code: string) {
  try {
    const q = await fetchQuote(code)
    tradePrice.value = q.price
    quoteCache.value.set(code, q)
  } catch { tradePrice.value = null }
}

function openTrade(mode: 'BUY' | 'SELL', code?: string, name?: string) {
  tradeMode.value = mode
  if (code && name) {
    tradeCode.value = code
    tradeName.value = name
    fetchCurrentPrice(code)
  } else {
    tradeCode.value = ''
    tradeName.value = ''
    tradePrice.value = null
  }
  tradeShares.value = 100
  searchKeyword.value = ''
  searchResults.value = []
  tradeDialog.value = true
}

async function executeTrade() {
  if (!tradeCode.value || !tradeShares.value) {
    ElMessage.warning('请选择股票并输入数量')
    return
  }
  try {
    const label = tradeMode.value === 'BUY' ? '买入' : '卖出'
    if (orderType.value === 'MARKET') {
      const fn = tradeMode.value === 'BUY' ? buyStock : sellStock
      const result = await fn(tradeCode.value, tradeShares.value)
      ElMessage.success(`${label}成功！${result.name} ${result.shares}股 @${fmt(result.price)}`)
    } else {
      await placeOrder(tradeCode.value, tradeMode.value, 'LIMIT', limitPrice.value ?? 0, tradeShares.value)
      ElMessage.success(`限价${label}委托已提交`)
    }
    tradeDialog.value = false
    await refreshData()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '交易失败')
  }
}

async function cancelOrderHandler(id: number) {
  try {
    await cancelOrder(id)
    ElMessage.success('已撤单')
    await loadOrders()
  } catch (e: unknown) { ElMessage.error(e instanceof Error ? e.message : '撤单失败') }
}

async function loadOrders() {
  try { orders.value = await fetchOrders() } catch { orders.value = [] }
}

function goStock(code: string) {
  router.push(`/tools/stock/${code}`)
}

function maxSellShares(code: string): number {
  const h = portfolio.value?.holdings.find((h) => h.code === code)
  return h?.shares ?? 0
}

let refreshInterval: ReturnType<typeof setInterval> | null = null

onMounted(async () => {
  await refreshData()
  refreshInterval = setInterval(refreshData, 30000)
})

onUnmounted(() => {
  if (refreshInterval) clearInterval(refreshInterval)
})
</script>

<template>
  <div class="stock-page">
    <h1 class="page-title">模拟炒股</h1>
    <p class="page-subtitle">虚拟资金 {{ INITIAL_CASH.toLocaleString() }} 元 · A股/美股/港股实时行情 · 仅供学习参考</p>

    <!-- 市场切换 + 热门股票 -->
    <div class="market-bar">
      <div class="market-tabs">
        <button v-for="(label, key) in MARKET_LABELS" :key="key"
          class="market-tab" :class="{ active: market === key }"
          @click="market = (key as Market)">{{ label }}</button>
      </div>
      <div class="popular-chips">
        <button v-for="s in POPULAR[market]" :key="s.code"
          class="popular-chip" @click="goStock(s.code)">{{ s.name }}</button>
      </div>
      <div v-if="hotStocks.length" class="hot-scroll">
        <button v-for="h in hotStocks" :key="h.code" class="hot-card" @click="goStock(h.code)">
          <span class="hot-name">{{ h.name }}</span>
          <span class="hot-price">{{ fmt(h.price, h.price < 1 ? 3 : 2) }}</span>
          <span :class="(h.changePct ?? 0) >= 0 ? 'pnl-up' : 'pnl-down'">
            {{ (h.changePct ?? 0) >= 0 ? '+' : '' }}{{ fmt(h.changePct) }}%
          </span>
        </button>
      </div>
    </div>

    <!-- 资产概览 -->
    <div v-if="portfolio" class="asset-bar">
      <div class="asset-item">
        <span class="asset-label">总资产</span>
        <span class="asset-value">{{ fmt(portfolio.totalAssets) }}</span>
      </div>
      <div class="asset-item">
        <span class="asset-label">可用资金</span>
        <span class="asset-value">{{ fmt(portfolio.cash) }}</span>
      </div>
      <div class="asset-item">
        <span class="asset-label">持仓市值</span>
        <span class="asset-value">{{ fmt(portfolio.marketValue) }}</span>
      </div>
      <div class="asset-item">
        <span class="asset-label">总盈亏</span>
        <span class="asset-value" :class="pnlClass(portfolio.totalPnl)">
          {{ fmt(portfolio.totalPnl) }} ({{ fmt(portfolio.totalPnlPct) }}%)
        </span>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="search-box">
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索股票代码或名称..."
          @input="onSearchInput"
        />
        <span v-if="searchLoading" class="search-spinner"></span>
        <div v-if="searchResults.length > 0" class="search-dropdown">
          <div
            v-for="item in searchResults"
            :key="item.fullCode"
            class="search-item"
            @click="selectStock(item)"
          >
            <span class="si-name">{{ item.name }}</span>
            <span class="si-code">{{ item.code }}</span>
          </div>
        </div>
      </div>
      <button class="btn btn-buy" @click="openTrade('BUY')">买入</button>
      <button class="btn btn-sell" @click="openTrade('SELL')">卖出</button>
    </div>

    <!-- Tab -->
    <div class="tabs">
      <button :class="{ active: activeTab === 'portfolio' }" @click="activeTab = 'portfolio'">持仓</button>
      <button :class="{ active: activeTab === 'trades' }" @click="activeTab = 'trades'">交易记录</button>
      <button :class="{ active: activeTab === 'leaderboard' }" @click="activeTab = 'leaderboard'">排行榜</button>
      <button :class="{ active: activeTab === 'orders' }" @click="activeTab = 'orders'; loadOrders()">委托</button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading">加载中...</div>

    <!-- 持仓列表 -->
    <div v-else-if="activeTab === 'portfolio'" class="table-wrap">
      <table v-if="portfolio && portfolio.holdings.length > 0">
        <thead>
          <tr>
            <th>股票</th><th>持仓</th><th>成本</th><th>现价</th><th>盈亏</th><th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="h in portfolio.holdings" :key="h.code">
            <td>
              <a class="stock-link" @click="router.push(`/tools/stock/${h.code}`)">{{ h.realName || h.name }}</a>
              <br><span class="code">{{ h.code }}</span>
            </td>
            <td>{{ h.shares }} 股</td>
            <td>{{ fmt(h.avgCost) }}</td>
            <td :class="h.currentPrice ? '' : 'muted'">{{ h.currentPrice ? fmt(h.currentPrice) : '--' }}</td>
            <td :class="pnlClass(h.pnl)">{{ fmt(h.pnl) }} ({{ fmt(h.pnlPct) }}%)</td>
            <td>
              <button class="mini-btn sell" @click="openTrade('SELL', h.code, h.realName || h.name)">卖</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="empty">暂无持仓</p>
    </div>

    <!-- 交易记录 -->
    <div v-else-if="activeTab === 'trades'" class="table-wrap">
      <table v-if="trades.length > 0">
        <thead>
          <tr><th>时间</th><th>股票</th><th>类型</th><th>数量</th><th>价格</th><th>手续费</th><th>盈亏</th></tr>
        </thead>
        <tbody>
          <tr v-for="t in trades" :key="t.time + t.code">
            <td class="time">{{ new Date(t.time).toLocaleString('zh-CN') }}</td>
            <td>{{ t.name }}<br><span class="code">{{ t.code }}</span></td>
            <td :class="t.type === 'BUY' ? 'type-buy' : 'type-sell'">{{ t.type === 'BUY' ? '买入' : '卖出' }}</td>
            <td>{{ t.shares }} 股</td>
            <td>{{ fmt(t.price) }}</td>
            <td>{{ fmt(t.fee) }}</td>
            <td :class="pnlClass(t.profitLoss)">{{ fmt(t.profitLoss) }}</td>
          </tr>
        </tbody>
      </table>
      <p v-else class="empty">暂无交易</p>
    </div>

    <!-- 委托列表 -->
    <div v-else-if="activeTab === 'orders'" class="table-wrap">
      <table v-if="orders.length > 0">
        <thead><tr><th>时间</th><th>股票</th><th>类型</th><th>委托价</th><th>数量</th><th>已成交</th><th>状态</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="o in orders" :key="o.id ?? o.createdAt">
            <td class="time">{{ new Date(o.createdAt).toLocaleString('zh-CN') }}</td>
            <td>{{ o.name }}<br><span class="code">{{ o.code }}</span></td>
            <td :class="o.type === 'BUY' ? 'type-buy' : 'type-sell'">{{ o.type === 'BUY' ? '买入' : '卖出' }}</td>
            <td>{{ fmt(o.price) }}</td>
            <td>{{ o.shares }} 股</td>
            <td>{{ o.filledShares }} 股</td>
            <td>
              <span :class="o.status === 'FILLED' ? 'type-buy' : o.status === 'CANCELLED' ? 'muted' : 'type-sell'">
                {{ o.status === 'PENDING' ? '待成交' : o.status === 'FILLED' ? '已成交' : o.status === 'PARTIAL' ? '部分成交' : '已撤单' }}
              </span>
            </td>
            <td>
              <button v-if="o.status === 'PENDING'" class="mini-btn cancel" @click="cancelOrderHandler(o.id!)">撤单</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="empty">暂无委托</p>
    </div>

    <!-- 排行榜 -->
    <div v-else class="table-wrap">
      <table v-if="leaderboard.length > 0">
        <thead><tr><th>排名</th><th>用户</th><th>总盈亏</th></tr></thead>
        <tbody>
          <tr v-for="(e, i) in leaderboard" :key="e.userId">
            <td>#{{ i + 1 }}</td>
            <td>用户 {{ e.userId }}</td>
            <td :class="pnlClass(e.totalPnl)">{{ fmt(e.totalPnl) }}</td>
          </tr>
        </tbody>
      </table>
      <p v-else class="empty">暂无排行数据</p>
    </div>

    <!-- 交易弹窗 -->
    <div v-if="tradeDialog" class="trade-overlay" @click.self="tradeDialog = false">
      <div class="trade-dialog">
        <h3>{{ tradeMode === 'BUY' ? '买入' : '卖出' }}股票</h3>
        <div class="trade-field">
          <label>股票</label>
          <div class="stock-pick">
            <input v-model="searchKeyword" type="text" :placeholder="tradeName || '搜索股票...'" @input="onSearchInput" />
            <div v-if="searchResults.length > 0" class="search-dropdown in-dialog">
              <div v-for="item in searchResults" :key="item.fullCode" class="search-item" @click="selectStock(item)">
                {{ item.name }} ({{ item.code }})
              </div>
            </div>
          </div>
        </div>
        <div class="trade-field">
          <label>委托类型</label>
          <div class="order-type-row">
            <button :class="{ active: orderType === 'LIMIT' }" @click="orderType = 'LIMIT'">限价单</button>
            <button :class="{ active: orderType === 'MARKET' }" @click="orderType = 'MARKET'">市价单</button>
          </div>
        </div>
        <div v-if="orderType === 'LIMIT'" class="trade-field">
          <label>委托价格</label>
          <input v-model.number="limitPrice" type="number" step="0.001" placeholder="指定价格" />
        </div>
        <div v-if="tradePrice" class="trade-field">
          <label>当前价格</label>
          <span class="price">{{ fmt(tradePrice, 3) }}</span>
        </div>
        <div class="trade-field">
          <label>{{ tradeMode === 'BUY' ? '买入' : '卖出' }}数量（股）</label>
          <div class="shares-row">
            <input v-model.number="tradeShares" type="number" min="100" step="100" />
            <span v-if="tradePrice" class="cost">
              约 {{ fmt(tradePrice * tradeShares) }} 元
              <span v-if="tradeMode === 'SELL' && tradeCode">
                （最大 {{ maxSellShares(tradeCode) }} 股）
              </span>
            </span>
          </div>
        </div>
        <div class="trade-actions">
          <button class="btn btn-primary" @click="executeTrade">
            {{ tradeMode === 'BUY' ? '确认买入' : '确认卖出' }}
          </button>
          <button class="btn btn-cancel" @click="tradeDialog = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stock-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 16px;
  color: var(--text-color);
}

.page-title { font-size: 1.5rem; font-weight: 700; margin: 0 0 4px; }
.page-subtitle { color: var(--text-muted); font-size: 0.85rem; margin: 0 0 24px; }

/* asset bar */
.asset-bar { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 20px; }
.asset-item {
  background: var(--surface-2);
  border: 1px solid var(--glass-border);
  border-radius: 12px;
  padding: 14px 16px;
}
.asset-label { display: block; font-size: 0.75rem; color: var(--text-muted); margin-bottom: 4px; }
.asset-value { font-size: 1.1rem; font-weight: 700; color: var(--text-color); }

/* market bar */
.market-bar { margin-bottom: 16px; }
.market-tabs { display: flex; gap: 4px; margin-bottom: 10px; }
.market-tab {
  background: var(--surface-2); border: 1px solid var(--glass-border); color: var(--text-muted);
  padding: 8px 18px; border-radius: 10px; cursor: pointer; font-size: 0.85rem; font-weight: 600; transition: all 0.2s;
}
.market-tab.active { background: var(--primary-color); color: #fff; border-color: transparent; }
.popular-chips { display: flex; flex-wrap: wrap; gap: 6px; }
.popular-chip {
  background: var(--surface-2); border: 1px solid var(--glass-border); color: var(--text-color);
  padding: 5px 12px; border-radius: 6px; cursor: pointer; font-size: 0.78rem; transition: all 0.15s;
}
.popular-chip:hover { border-color: var(--primary-color); color: var(--primary-color); }
.hot-scroll { display: flex; gap: 8px; overflow-x: auto; padding-bottom: 4px; margin-top: 8px; -webkit-overflow-scrolling: touch; scrollbar-width: none; }
.hot-scroll::-webkit-scrollbar { display: none; }
.hot-card {
  flex-shrink: 0; display: flex; flex-direction: column; align-items: center; gap: 2px;
  padding: 8px 14px; border-radius: 10px; border: 1px solid var(--glass-border);
  background: var(--surface-2); cursor: pointer; transition: all 0.15s; min-width: 90px;
}
.hot-card:hover { border-color: var(--primary-color); transform: translateY(-2px); }
.hot-name { font-size: 0.75rem; color: var(--text-muted); white-space: nowrap; }
.hot-price { font-size: 0.9rem; font-weight: 700; color: var(--text-color); }

/* action bar */
.action-bar { display: flex; gap: 10px; margin-bottom: 16px; align-items: center; }
.search-box { flex: 1; position: relative; }

.search-box input {
  width: 100%; padding: 10px 14px; border-radius: 10px;
  border: 1px solid var(--glass-border);
  background: var(--surface-2);
  color: var(--text-color);
  font-size: 0.9rem; outline: none;
}
.search-box input::placeholder { color: var(--text-muted); }
.search-box input:focus { border-color: var(--primary-color); }

.search-spinner {
  position: absolute; right: 10px; top: 50%; transform: translateY(-50%);
  width: 14px; height: 14px;
  border: 2px solid var(--glass-border);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: translateY(-50%) rotate(360deg); } }

.search-dropdown {
  position: absolute; top: 100%; left: 0; right: 0;
  background: var(--surface-elevated);
  border: 1px solid var(--glass-border);
  border-radius: 10px; max-height: 200px; overflow: auto; z-index: 100; margin-top: 4px;
}
.search-dropdown.in-dialog { position: relative; }
.search-item {
  padding: 10px 14px; cursor: pointer; display: flex;
  justify-content: space-between; font-size: 0.9rem; transition: background 0.15s;
  color: var(--text-color);
}
.search-item:hover { background: var(--surface-3); }
.si-code { color: var(--text-muted); font-size: 0.8rem; }

/* buttons */
.btn {
  padding: 10px 20px; border-radius: 10px; border: none;
  font-weight: 600; font-size: 0.9rem; cursor: pointer; transition: all 0.2s;
}
.btn-buy { background: rgba(239, 68, 68, 0.15); color: #dc2626; border: 1px solid rgba(239, 68, 68, 0.3); }
.btn-buy:hover { background: rgba(239, 68, 68, 0.28); }
.btn-sell { background: rgba(34, 197, 94, 0.15); color: #16a34a; border: 1px solid rgba(34, 197, 94, 0.3); }
.btn-sell:hover { background: rgba(34, 197, 94, 0.28); }
.btn-primary { background: var(--primary-color); color: #fff; border: none; width: 100%; }
.btn-primary:hover { opacity: 0.88; }
.btn-cancel {
  background: var(--surface-2); color: var(--text-muted);
  border: 1px solid var(--glass-border); width: 100%;
}

/* tabs */
.tabs { display: flex; gap: 4px; margin-bottom: 16px; border-bottom: 1px solid var(--glass-border); }
.tabs button {
  background: none; border: none; color: var(--text-muted);
  padding: 10px 18px; cursor: pointer; font-size: 0.9rem;
  border-bottom: 2px solid transparent; transition: all 0.2s;
}
.tabs button.active { color: var(--primary-color); border-bottom-color: var(--primary-color); }

/* tables */
.table-wrap { overflow-x: auto; }
.table-wrap table {
  width: 100%; border-collapse: collapse;
  background: var(--surface-2);
  border-radius: 12px; overflow: hidden;
}
th, td { text-align: left; padding: 10px 12px; border-bottom: 1px solid var(--glass-border); font-size: 0.85rem; }
th { color: var(--text-muted); font-weight: 600; }
td { color: var(--text-color); }
.code { color: var(--text-muted); font-size: 0.75rem; }

.stock-link { color: var(--primary-color); cursor: pointer; font-weight: 600; text-decoration: none; }
.stock-link:hover { text-decoration: underline; }

.mini-btn { padding: 4px 10px; border-radius: 6px; border: none; font-size: 0.75rem; cursor: pointer; }
.mini-btn.sell { background: rgba(34, 197, 94, 0.15); color: #16a34a; }
.mini-btn.cancel { background: rgba(239, 68, 68, 0.12); color: #dc2626; }

.order-type-row { display: flex; gap: 6px; }
.order-type-row button {
  flex: 1; padding: 8px; border-radius: 8px; border: 1px solid var(--glass-border);
  background: var(--surface-2); color: var(--text-muted); font-size: 0.82rem; cursor: pointer; transition: all 0.15s;
}
.order-type-row button.active { background: var(--primary-color); color: #fff; border-color: transparent; }

.pnl-up { color: #dc2626 !important; }
.pnl-down { color: #16a34a !important; }
.pnl-zero { color: var(--text-muted); }
.type-buy { color: #dc2626; }
.type-sell { color: #16a34a; }
.muted { color: var(--text-muted); }
.time { white-space: nowrap; font-size: 0.8rem; color: var(--text-muted); }
.empty { text-align: center; padding: 40px; color: var(--text-muted); }
.loading { text-align: center; padding: 40px; color: var(--text-muted); }

/* dialog */
.trade-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center; z-index: 2000;
}
.trade-dialog {
  background: var(--surface-elevated);
  border: 1px solid var(--glass-border);
  border-radius: 16px; padding: 24px; width: 400px; max-width: 90vw;
  color: var(--text-color);
}
.trade-dialog h3 { margin: 0 0 16px; }
.trade-field { margin-bottom: 14px; }
.trade-field label { display: block; font-size: 0.8rem; color: var(--text-muted); margin-bottom: 4px; }
.trade-field input, .stock-pick input {
  width: 100%; padding: 10px 12px; border-radius: 8px;
  border: 1px solid var(--glass-border);
  background: var(--surface-1);
  color: var(--text-color);
  font-size: 0.9rem; outline: none;
}
.trade-field input:focus, .stock-pick input:focus { border-color: var(--primary-color); }
.stock-pick { position: relative; }
.price { font-size: 1.2rem; font-weight: 700; }
.shares-row { display: flex; gap: 10px; align-items: center; }
.shares-row input { width: 120px; }
.cost { font-size: 0.8rem; color: var(--text-muted); }
.trade-actions { display: flex; flex-direction: column; gap: 8px; margin-top: 20px; }

@media (max-width: 640px) {
  .asset-bar { grid-template-columns: repeat(2, 1fr); }
  .action-bar { flex-wrap: wrap; }
}
</style>
