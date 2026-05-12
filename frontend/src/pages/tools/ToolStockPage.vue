<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  fetchQuote, searchStocks, fetchPortfolio, fetchTrades, fetchLeaderboard,
  buyStock, sellStock,
  type StockQuote, type StockSearchResult, type PortfolioSummary,
  type TradeHistoryItem, type LeaderboardEntry,
} from '../../api/stock'

const quoteCache = ref<Map<string, StockQuote>>(new Map())

const searchKeyword = ref('')
const searchResults = ref<StockSearchResult[]>([])
const searchLoading = ref(false)
let searchTimer: ReturnType<typeof setTimeout> | null = null

const portfolio = ref<PortfolioSummary | null>(null)
const trades = ref<TradeHistoryItem[]>([])
const leaderboard = ref<LeaderboardEntry[]>([])
const activeTab = ref<'portfolio' | 'trades' | 'leaderboard'>('portfolio')

const tradeCode = ref('')
const tradeName = ref('')
const tradePrice = ref<number | null>(null)
const tradeShares = ref(100)
const tradeDialog = ref(false)
const tradeMode = ref<'BUY' | 'SELL'>('BUY')

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
    const fn = tradeMode.value === 'BUY' ? buyStock : sellStock
    const result = await fn(tradeCode.value, tradeShares.value)
    const label = tradeMode.value === 'BUY' ? '买入' : '卖出'
    ElMessage.success(`${label}成功！${result.name} ${result.shares}股 @${fmt(result.price)}`)
    tradeDialog.value = false
    await refreshData()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '交易失败')
  }
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
    <p class="page-subtitle">虚拟资金 {{ INITIAL_CASH.toLocaleString() }} 元 · 数据来自新浪财经 · 仅供学习参考</p>

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
            <td>{{ h.realName || h.name }}<br><span class="code">{{ h.code }}</span></td>
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
  color: var(--text-color, #f1f5f9);
}

.page-title {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0 0 4px;
}

.page-subtitle {
  color: var(--text-muted, #94a3b8);
  font-size: 0.85rem;
  margin: 0 0 24px;
}

/* asset bar */
.asset-bar {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.asset-item {
  background: var(--glass-bg, rgba(30, 30, 30, 0.8));
  border: 1px solid var(--glass-border, rgba(255,255,255,0.1));
  border-radius: 12px;
  padding: 14px 16px;
}

.asset-label { display: block; font-size: 0.75rem; color: var(--text-muted, #94a3b8); margin-bottom: 4px; }
.asset-value { font-size: 1.1rem; font-weight: 700; }

/* action bar */
.action-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  align-items: center;
}

.search-box {
  flex: 1;
  position: relative;
}

.search-box input {
  width: 100%;
  padding: 10px 14px;
  border-radius: 10px;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.15));
  background: var(--glass-bg, rgba(30,30,30,0.8));
  color: var(--text-color, #f1f5f9);
  font-size: 0.9rem;
  outline: none;
}

.search-spinner {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.2);
  border-top-color: var(--primary-color, #60a5fa);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin { to { transform: translateY(-50%) rotate(360deg); } }

.search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: rgba(20, 24, 36, 0.98);
  border: 1px solid rgba(255,255,255,0.15);
  border-radius: 10px;
  max-height: 200px;
  overflow: auto;
  z-index: 100;
  margin-top: 4px;
}

.search-dropdown.in-dialog { position: relative; }

.search-item {
  padding: 10px 14px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
  transition: background 0.15s;
}
.search-item:hover { background: rgba(255,255,255,0.08); }
.si-code { color: var(--text-muted, #94a3b8); font-size: 0.8rem; }

.btn {
  padding: 10px 20px;
  border-radius: 10px;
  border: none;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-buy { background: rgba(239, 68, 68, 0.18); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.3); }
.btn-buy:hover { background: rgba(239, 68, 68, 0.3); }
.btn-sell { background: rgba(34, 197, 94, 0.18); color: #4ade80; border: 1px solid rgba(34, 197, 94, 0.3); }
.btn-sell:hover { background: rgba(34, 197, 94, 0.3); }
.btn-primary { background: rgba(59, 130, 246, 0.2); color: #60a5fa; border: 1px solid rgba(59, 130, 246, 0.3); width: 100%; }
.btn-primary:hover { background: rgba(59, 130, 246, 0.35); }
.btn-cancel { background: rgba(255,255,255,0.05); color: var(--text-muted, #94a3b8); border: 1px solid rgba(255,255,255,0.1); width: 100%; }

/* tabs */
.tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 16px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.tabs button {
  background: none;
  border: none;
  color: var(--text-muted, #94a3b8);
  padding: 10px 18px;
  cursor: pointer;
  font-size: 0.9rem;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}
.tabs button.active { color: var(--primary-color, #60a5fa); border-bottom-color: var(--primary-color, #60a5fa); }

/* tables */
.table-wrap { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
th, td { text-align: left; padding: 10px 12px; border-bottom: 1px solid rgba(255,255,255,0.06); font-size: 0.85rem; }
th { color: var(--text-muted, #94a3b8); font-weight: 600; }
.code { color: var(--text-muted, #94a3b8); font-size: 0.75rem; }

.mini-btn { padding: 4px 10px; border-radius: 6px; border: none; font-size: 0.75rem; cursor: pointer; }
.mini-btn.sell { background: rgba(34, 197, 94, 0.15); color: #4ade80; }

.pnl-up { color: #f87171 !important; }
.pnl-down { color: #4ade80 !important; }
.pnl-zero { color: var(--text-muted, #94a3b8); }
.type-buy { color: #f87171; }
.type-sell { color: #4ade80; }
.muted { color: var(--text-muted, #94a3b8); }
.time { white-space: nowrap; font-size: 0.8rem; color: var(--text-muted, #94a3b8); }

.empty { text-align: center; padding: 40px; color: var(--text-muted, #94a3b8); }
.loading { text-align: center; padding: 40px; }

/* dialog */
.trade-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}
.trade-dialog {
  background: rgba(20, 24, 40, 0.98);
  border: 1px solid rgba(255,255,255,0.15);
  border-radius: 16px;
  padding: 24px;
  width: 400px;
  max-width: 90vw;
}
.trade-dialog h3 { margin: 0 0 16px; }
.trade-field { margin-bottom: 14px; }
.trade-field label { display: block; font-size: 0.8rem; color: var(--text-muted, #94a3b8); margin-bottom: 4px; }
.trade-field input, .stock-pick input {
  width: 100%;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid rgba(255,255,255,0.15);
  background: rgba(255,255,255,0.05);
  color: var(--text-color, #f1f5f9);
  font-size: 0.9rem;
  outline: none;
}
.stock-pick { position: relative; }
.price { font-size: 1.2rem; font-weight: 700; }
.shares-row { display: flex; gap: 10px; align-items: center; }
.shares-row input { width: 120px; }
.cost { font-size: 0.8rem; color: var(--text-muted, #94a3b8); }
.trade-actions { display: flex; flex-direction: column; gap: 8px; margin-top: 20px; }

@media (max-width: 640px) {
  .asset-bar { grid-template-columns: repeat(2, 1fr); }
  .action-bar { flex-wrap: wrap; }
}
</style>
