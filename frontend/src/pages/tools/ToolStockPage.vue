<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  fetchQuote, searchStocks, fetchPortfolio, fetchTrades, fetchLeaderboard,
  buyStock, sellStock, fetchHotStocks, placeOrder, cancelOrder, fetchOrders,
  type StockQuote, type StockSearchResult, type PortfolioSummary,
  type TradeHistoryItem, type LeaderboardEntry, type HotStockItem, type OrderItem,
  type TradeResult,
} from '../../api/stock'

type Market = 'cn' | 'us' | 'hk'

const router = useRouter()

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

/** 根据代码判断所属市场（与后端 Yahoo / 沪深规则一致） */
function inferMarketFromCode(code: string): Market {
  const c = code.trim().toLowerCase()
  if (/^(sh|sz)\d{6}$/.test(c)) return 'cn'
  if (c.endsWith('.hk')) return 'hk'
  return 'us'
}

const market = ref<Market>('cn')
const hotStocks = ref<HotStockItem[]>([])

watch(market, async (m) => {
  clearPagePick()
  pageResults.value = []
  try { hotStocks.value = await fetchHotStocks(m) } catch { hotStocks.value = [] }
}, { immediate: true })
const quoteCache = ref<Map<string, StockQuote>>(new Map())

/** 主页搜索框（与弹窗搜索完全分离，避免点买入后清空导致「没有股票」） */
const pageQuery = ref('')
const pageResults = ref<StockSearchResult[]>([])
const pageSearchLoading = ref(false)
let pageSearchTimer: ReturnType<typeof setTimeout> | null = null
/** 在主页搜索结果中点选的股票，买入/卖出须先选此项 */
const pickedStock = ref<StockSearchResult | null>(null)
const pickedQuotePrice = ref<number | null>(null)

/** 弹窗内更换股票时的搜索 */
const dialogQuery = ref('')
const dialogResults = ref<StockSearchResult[]>([])
const dialogSearchLoading = ref(false)
let dialogSearchTimer: ReturnType<typeof setTimeout> | null = null

const portfolio = ref<PortfolioSummary | null>(null)
const trades = ref<TradeHistoryItem[]>([])
const leaderboard = ref<LeaderboardEntry[]>([])
// activeTab declared below with orders support

/** 当前市场下的持仓 / 成交 / 委托（多市场共用同一模拟资金账户） */
const filteredHoldings = computed(() => {
  const p = portfolio.value
  if (!p?.holdings?.length) return []
  return p.holdings.filter((h) => inferMarketFromCode(h.code) === market.value)
})

const filteredTrades = computed(() =>
  trades.value.filter((t) => inferMarketFromCode(t.code) === market.value),
)

const filteredOrders = computed(() =>
  orders.value.filter((o) => inferMarketFromCode(o.code) === market.value),
)

const segmentMarketValue = computed(() =>
  filteredHoldings.value.reduce((sum, h) => {
    const px = h.currentPrice != null && h.currentPrice > 0 ? h.currentPrice : h.avgCost
    return sum + px * h.shares
  }, 0),
)

const segmentCost = computed(() =>
  filteredHoldings.value.reduce((s, h) => s + h.avgCost * h.shares, 0),
)

const segmentPnl = computed(() => filteredHoldings.value.reduce((s, h) => s + h.pnl, 0))

const segmentPnlPct = computed(() => {
  const c = segmentCost.value
  if (c <= 0) return 0
  return (segmentPnl.value / c) * 100
})

const searchPlaceholder = computed(() => {
  switch (market.value) {
    case 'us':
      return '搜索美股代码或名称（如 AAPL、Apple），在列表中点选…'
    case 'hk':
      return '搜索港股代码或名称（如 0700、腾讯），在列表中点选…'
    default:
      return '输入代码或名称搜索 A 股，在下方列表中点选股票…'
  }
})

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
const FEE_RATE = 0.0003

function fmt(n: number | null | undefined, decimals = 2): string {
  if (n == null) return '--'
  return Number(n).toFixed(decimals)
}

function isCnSixDigitCode(code: string): boolean {
  const c = code.trim().toLowerCase()
  return /^(sh|sz)\d{6}$/.test(c)
}

function estFeeFromGross(gross: number): number {
  return Math.max(5, Math.round(gross * FEE_RATE * 1000) / 1000)
}

function tradeUnitPrice(): number | null {
  if (orderType.value === 'LIMIT') {
    const lp = limitPrice.value
    if (lp != null && lp > 0) return lp
  }
  return tradePrice.value
}

const tradeGrossPreview = computed(() => {
  const px = tradeUnitPrice()
  if (px == null || !tradeShares.value) return null
  return px * tradeShares.value
})

const tradeFeePreview = computed(() => {
  const g = tradeGrossPreview.value
  if (g == null) return null
  return estFeeFromGross(g)
})

const tradeDebitPreview = computed(() => {
  if (tradeMode.value !== 'BUY') return null
  const g = tradeGrossPreview.value
  const f = tradeFeePreview.value
  if (g == null || f == null) return null
  return g + f
})

const tradeCreditPreview = computed(() => {
  if (tradeMode.value !== 'SELL') return null
  const g = tradeGrossPreview.value
  const f = tradeFeePreview.value
  if (g == null || f == null) return null
  return g - f
})

watch(orderType, (ot) => {
  if (ot === 'LIMIT' && tradePrice.value != null) limitPrice.value = tradePrice.value
})

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

function clearPagePick() {
  pickedStock.value = null
  pickedQuotePrice.value = null
  pageQuery.value = ''
}

function onPageSearchInput() {
  pickedStock.value = null
  pickedQuotePrice.value = null
  if (pageSearchTimer) clearTimeout(pageSearchTimer)
  const kw = pageQuery.value.trim()
  if (!kw) {
    pageResults.value = []
    return
  }
  pageSearchTimer = setTimeout(async () => {
    pageSearchLoading.value = true
    try {
      pageResults.value = await searchStocks(kw, market.value)
    } catch {
      pageResults.value = []
    } finally {
      pageSearchLoading.value = false
    }
  }, 220)
}

async function selectStockFromPage(item: StockSearchResult) {
  pickedStock.value = item
  pageQuery.value = `${item.name}（${item.code}）· ${item.fullCode}`
  pageResults.value = []
  try {
    const q = await fetchQuote(item.fullCode)
    pickedQuotePrice.value = q.price
    quoteCache.value.set(item.fullCode, q)
  } catch {
    pickedQuotePrice.value = null
  }
}

function onDialogSearchInput() {
  if (dialogSearchTimer) clearTimeout(dialogSearchTimer)
  const kw = dialogQuery.value.trim()
  if (!kw) {
    dialogResults.value = []
    return
  }
  dialogSearchTimer = setTimeout(async () => {
    dialogSearchLoading.value = true
    try {
      dialogResults.value = await searchStocks(kw, market.value)
    } catch {
      dialogResults.value = []
    } finally {
      dialogSearchLoading.value = false
    }
  }, 220)
}

async function selectStockFromDialog(item: StockSearchResult) {
  tradeCode.value = item.fullCode
  tradeName.value = item.name
  dialogQuery.value = `${item.name}（${item.code}）`
  dialogResults.value = []
  await fetchCurrentPrice(item.fullCode)
}

async function fetchCurrentPrice(code: string) {
  try {
    const q = await fetchQuote(code)
    tradePrice.value = q.price
    if (orderType.value === 'LIMIT') limitPrice.value = q.price
    quoteCache.value.set(code, q)
  } catch { tradePrice.value = null }
}

async function openTradeDialog(mode: 'BUY' | 'SELL', code: string, name: string) {
  tradeMode.value = mode
  limitPrice.value = null
  tradeShares.value = 100
  dialogQuery.value = ''
  dialogResults.value = []
  tradeCode.value = code
  tradeName.value = name
  tradeDialog.value = true
  await fetchCurrentPrice(code)
}

/** 主页买入/卖出：必须先点选搜索结果 */
function openTradeFromBar(mode: 'BUY' | 'SELL') {
  if (!pickedStock.value) {
    ElMessage.warning('请先在搜索框输入关键词，在下方列表中点击选择一只股票，再点买入或卖出')
    return
  }
  void openTradeDialog(mode, pickedStock.value.fullCode, pickedStock.value.name)
}

function openTradeFromHolding(mode: 'BUY' | 'SELL', code: string, name: string) {
  void openTradeDialog(mode, code, name)
}

function clearTradeStockInDialog() {
  tradeCode.value = ''
  tradeName.value = ''
  tradePrice.value = null
  limitPrice.value = null
  dialogQuery.value = ''
  dialogResults.value = []
}

async function executeTrade() {
  if (!tradeCode.value || !tradeShares.value) {
    ElMessage.warning('请选择股票并输入数量')
    return
  }
  if (isCnSixDigitCode(tradeCode.value)) {
    const s = tradeShares.value
    if (s < 100 || s % 100 !== 0) {
      ElMessage.warning('沪深A股委托数量须为100股的整数倍')
      return
    }
  }
  if (orderType.value === 'LIMIT') {
    const lp = limitPrice.value
    if (lp == null || lp <= 0) {
      ElMessage.warning('请输入有效的限价')
      return
    }
  }
  try {
    const label = tradeMode.value === 'BUY' ? '买入' : '卖出'
    if (orderType.value === 'MARKET') {
      const fn = tradeMode.value === 'BUY' ? buyStock : sellStock
      const result: TradeResult = await fn(tradeCode.value, tradeShares.value)
      const gross = result.grossAmount != null ? fmt(result.grossAmount) : fmt(result.price * result.shares)
      ElMessage.success(
        `${label}成交：${result.name} ${result.shares} 股 @${fmt(result.price)}，成交额 ${gross} 元，佣金 ${fmt(result.fee)}，资金余额 ${fmt(result.cashAfter)}`,
      )
    } else {
      await placeOrder(tradeCode.value, tradeMode.value, 'LIMIT', limitPrice.value ?? 0, tradeShares.value)
      ElMessage.success(`限价${label}委托已提交（当前市场为模拟撮合，满足价格条件后成交）`)
    }
    tradeDialog.value = false
    clearPagePick()
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

function roundLotDown(shares: number, code: string): number {
  if (!isCnSixDigitCode(code)) return Math.max(0, Math.floor(shares))
  return Math.max(0, Math.floor(shares / 100) * 100)
}

function setSellFraction(frac: number) {
  const max = maxSellShares(tradeCode.value)
  const q = roundLotDown(Math.floor(max * frac), tradeCode.value)
  if (q <= 0) {
    ElMessage.warning('当前无可卖数量')
    return
  }
  tradeShares.value = q
}

function maxBuySharesNow(): number {
  if (!portfolio.value) return 0
  const cash = portfolio.value.cash
  const px = tradeUnitPrice()
  if (px == null || px <= 0) return 0
  let hi = Math.floor(cash / px)
  hi = roundLotDown(hi, tradeCode.value)
  const step = isCnSixDigitCode(tradeCode.value) ? 100 : 1
  const minLot = isCnSixDigitCode(tradeCode.value) ? 100 : 1
  for (let s = hi; s >= minLot; s -= step) {
    const g = px * s
    const fee = estFeeFromGross(g)
    if (g + fee <= cash + 1e-6) return s
  }
  return 0
}

function applyMaxBuy() {
  const n = maxBuySharesNow()
  if (n <= 0) {
    ElMessage.warning('可用资金不足以按当前价格买入最小单位')
    return
  }
  tradeShares.value = n
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
    <p class="page-subtitle">
      虚拟资金 {{ INITIAL_CASH.toLocaleString() }} 元 · A股/美股/港股行情 · 总资产与资金为<strong>全市场合并</strong>的模拟账户；
      美股/港股价格为数据源币种，撮合按<strong>数值</strong>计入同一账户（仅供学习）
    </p>

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
        <span class="asset-label">总资产（全账户）</span>
        <span class="asset-value">{{ fmt(portfolio.totalAssets) }}</span>
      </div>
      <div class="asset-item">
        <span class="asset-label">可用资金</span>
        <span class="asset-value">{{ fmt(portfolio.cash) }}</span>
      </div>
      <div class="asset-item">
        <span class="asset-label">持仓市值（{{ MARKET_LABELS[market] }}）</span>
        <span class="asset-value">{{ fmt(segmentMarketValue) }}</span>
      </div>
      <div class="asset-item">
        <span class="asset-label">持仓盈亏（{{ MARKET_LABELS[market] }}）</span>
        <span class="asset-value" :class="pnlClass(segmentPnl)">
          {{ fmt(segmentPnl) }} ({{ fmt(segmentPnlPct) }}%)
        </span>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="search-box">
        <input
          v-model="pageQuery"
          type="text"
          :placeholder="searchPlaceholder"
          @input="onPageSearchInput"
        />
        <span v-if="pageSearchLoading" class="search-spinner"></span>
        <div v-if="pageResults.length > 0" class="search-dropdown">
          <div
            v-for="item in pageResults"
            :key="item.fullCode"
            class="search-item"
            @mousedown.prevent="selectStockFromPage(item)"
          >
            <span class="si-name">{{ item.name }}</span>
            <span class="si-meta">{{ item.fullCode }}</span>
          </div>
        </div>
      </div>
      <button class="btn btn-buy" type="button" @click="openTradeFromBar('BUY')">买入</button>
      <button class="btn btn-sell" type="button" @click="openTradeFromBar('SELL')">卖出</button>
    </div>
    <div v-if="pickedStock" class="picked-strip">
      <span class="picked-label">已选股票</span>
      <strong>{{ pickedStock.name }}</strong>
      <span class="code">{{ pickedStock.fullCode }}</span>
      <span v-if="pickedQuotePrice != null" class="picked-quote">现价 {{ fmt(pickedQuotePrice, pickedQuotePrice < 1 ? 3 : 2) }}</span>
      <button type="button" class="picked-clear" @click="clearPagePick()">清除</button>
    </div>
    <p v-else class="search-hint">搜索后点选列表中的一条，再点「买入」或「卖出」。</p>

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
      <table v-if="filteredHoldings.length > 0">
        <thead>
          <tr>
            <th>股票</th><th>持仓</th><th>成本</th><th>现价</th><th>盈亏</th><th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="h in filteredHoldings" :key="h.code">
            <td>
              <a class="stock-link" href="#" @click.prevent="goStock(h.code)">{{ h.realName || h.name }}</a>
              <br><span class="code">{{ h.code }}</span>
            </td>
            <td>{{ h.shares }} 股</td>
            <td>{{ fmt(h.avgCost) }}</td>
            <td :class="h.currentPrice ? '' : 'muted'">{{ h.currentPrice ? fmt(h.currentPrice) : '--' }}</td>
            <td :class="pnlClass(h.pnl)">{{ fmt(h.pnl) }} ({{ fmt(h.pnlPct) }}%)</td>
            <td>
              <button class="mini-btn sell" type="button" @click="openTradeFromHolding('SELL', h.code, h.realName || h.name)">卖</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="empty">当前{{ MARKET_LABELS[market] }}暂无持仓</p>
    </div>

    <!-- 交易记录 -->
    <div v-else-if="activeTab === 'trades'" class="table-wrap">
      <table v-if="filteredTrades.length > 0">
        <thead>
          <tr><th>时间</th><th>股票</th><th>类型</th><th>数量</th><th>价格</th><th>手续费</th><th>盈亏</th></tr>
        </thead>
        <tbody>
          <tr v-for="t in filteredTrades" :key="t.time + t.code">
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
      <p v-else class="empty">当前{{ MARKET_LABELS[market] }}暂无成交记录</p>
    </div>

    <!-- 委托列表 -->
    <div v-else-if="activeTab === 'orders'" class="table-wrap">
      <table v-if="filteredOrders.length > 0">
        <thead><tr><th>时间</th><th>股票</th><th>类型</th><th>委托价</th><th>数量</th><th>已成交</th><th>状态</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="o in filteredOrders" :key="o.id ?? o.createdAt">
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
      <p v-else class="empty">当前{{ MARKET_LABELS[market] }}暂无委托</p>
    </div>

    <!-- 排行榜 -->
    <div v-else class="table-wrap">
      <table v-if="leaderboard.length > 0">
        <thead><tr><th>排名</th><th>用户</th><th>总盈亏</th></tr></thead>
        <tbody>
          <tr v-for="(e, i) in leaderboard" :key="e.userId">
            <td>#{{ i + 1 }}</td>
            <td>{{ e.displayName || `用户 ${e.userId}` }}</td>
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
          <div v-if="tradeCode" class="dialog-stock-line">
            <div>
              <strong>{{ tradeName }}</strong>
              <span class="code">{{ tradeCode }}</span>
            </div>
            <button type="button" class="mini-link" @click="clearTradeStockInDialog">更换股票</button>
          </div>
          <div v-else class="stock-pick">
            <input
              v-model="dialogQuery"
              type="text"
              placeholder="搜索代码或名称…"
              @input="onDialogSearchInput"
            />
            <span v-if="dialogSearchLoading" class="search-spinner"></span>
            <div v-if="dialogResults.length > 0" class="search-dropdown in-dialog">
              <div
                v-for="item in dialogResults"
                :key="item.fullCode"
                class="search-item"
                @mousedown.prevent="selectStockFromDialog(item)"
              >
                <span class="si-name">{{ item.name }}</span>
                <span class="si-meta">{{ item.fullCode }}</span>
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
            <input
              v-model.number="tradeShares"
              type="number"
              :min="isCnSixDigitCode(tradeCode) ? 100 : 1"
              :step="isCnSixDigitCode(tradeCode) ? 100 : 1"
            />
            <span v-if="tradePrice" class="cost">
              约 {{ fmt(tradePrice * tradeShares) }} 元
              <span v-if="tradeMode === 'SELL' && tradeCode">
                （最大 {{ maxSellShares(tradeCode) }} 股）
              </span>
            </span>
          </div>
          <p v-if="isCnSixDigitCode(tradeCode)" class="lot-hint">沪深A股：每笔委托须为 100 股的整数倍（1 手 = 100 股）</p>
          <div
            v-if="tradeGrossPreview != null && tradeFeePreview != null && tradeMode === 'BUY' && tradeDebitPreview != null"
            class="trade-preview"
          >
            预计冻结：成交额 {{ fmt(tradeGrossPreview) }} + 佣金约 {{ fmt(tradeFeePreview) }}
            ≈ <strong>{{ fmt(tradeDebitPreview) }}</strong> 元（与实盘规则类似：万三佣金、单笔最低 5 元）
          </div>
          <div
            v-else-if="tradeGrossPreview != null && tradeFeePreview != null && tradeMode === 'SELL' && tradeCreditPreview != null"
            class="trade-preview"
          >
            预计卖出回款：成交额 {{ fmt(tradeGrossPreview) }} − 佣金约 {{ fmt(tradeFeePreview) }}
            ≈ <strong>{{ fmt(tradeCreditPreview) }}</strong> 元
          </div>
          <div class="shares-quick">
            <template v-if="tradeMode === 'BUY'">
              <button type="button" class="qty-chip" @click="applyMaxBuy">按资金最大可买</button>
            </template>
            <template v-else>
              <button type="button" class="qty-chip" @click="setSellFraction(1)">全仓</button>
              <button type="button" class="qty-chip" @click="setSellFraction(0.5)">半仓</button>
              <button type="button" class="qty-chip" @click="setSellFraction(0.25)">1/4 仓</button>
            </template>
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
.si-meta { color: var(--text-muted); font-size: 0.78rem; font-family: ui-monospace, monospace; }

.picked-strip {
  display: flex; flex-wrap: wrap; align-items: center; gap: 8px 12px;
  padding: 10px 14px; margin-bottom: 14px; border-radius: 10px;
  border: 1px solid var(--glass-border); background: var(--surface-2); font-size: 0.88rem;
}
.picked-label { color: var(--text-muted); font-size: 0.78rem; }
.picked-quote { color: var(--text-color); font-weight: 600; }
.picked-clear {
  margin-left: auto; padding: 4px 10px; border-radius: 6px; border: 1px solid var(--glass-border);
  background: var(--surface-1); color: var(--text-muted); font-size: 0.78rem; cursor: pointer;
}
.picked-clear:hover { border-color: var(--primary-color); color: var(--primary-color); }
.search-hint { margin: -8px 0 16px; font-size: 0.8rem; color: var(--text-muted); }

.dialog-stock-line {
  display: flex; align-items: flex-start; justify-content: space-between; gap: 12px;
  padding: 10px 12px; border-radius: 8px; border: 1px solid var(--glass-border); background: var(--surface-1);
}
.mini-link {
  flex-shrink: 0; padding: 0; border: none; background: none; color: var(--primary-color);
  font-size: 0.82rem; font-weight: 600; cursor: pointer; text-decoration: underline;
}
.mini-link:hover { opacity: 0.85; }

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
.lot-hint { margin: 6px 0 0; font-size: 0.75rem; color: var(--text-muted); }
.trade-preview { margin-top: 8px; font-size: 0.78rem; color: var(--text-muted); line-height: 1.45; }
.trade-preview strong { color: var(--text-color); }
.shares-quick { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 10px; }
.qty-chip {
  padding: 5px 10px; border-radius: 6px; border: 1px solid var(--glass-border);
  background: var(--surface-2); color: var(--text-color); font-size: 0.75rem; cursor: pointer; transition: all 0.15s;
}
.qty-chip:hover { border-color: var(--primary-color); color: var(--primary-color); }
.trade-actions { display: flex; flex-direction: column; gap: 8px; margin-top: 20px; }

@media (max-width: 640px) {
  .asset-bar { grid-template-columns: repeat(2, 1fr); }
  .action-bar { flex-wrap: wrap; }
}
</style>
