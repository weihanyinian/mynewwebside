<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CandlestickChart, LineChart, BarChart, PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, GridComponent, DataZoomComponent, LegendComponent, MarkLineComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import {
  fetchQuote, fetchIntraday, fetchKline, fetchCapitalFlow,
  buyStock, sellStock,
  type StockQuote, type IntradayData, type KlineData, type CapitalFlow,
} from '../../api/stock'

use([CandlestickChart, LineChart, BarChart, PieChart, TitleComponent, TooltipComponent, GridComponent, DataZoomComponent, LegendComponent, MarkLineComponent, CanvasRenderer])

const route = useRoute()
const router = useRouter()

const code = computed(() => route.params.code as string)
const quote = ref<StockQuote | null>(null)
const intraday = ref<IntradayData | null>(null)
const kline = ref<KlineData | null>(null)
const capitalFlow = ref<CapitalFlow | null>(null)
const klinePeriod = ref<'day' | 'week' | 'month'>('day')
const loading = ref(true)
const tradeShares = ref(100)
const showBuy = ref(false)
const showSell = ref(false)

const changeClass = computed(() => {
  if (!quote.value) return ''
  return (quote.value.changePct ?? 0) >= 0 ? 'up' : 'down'
})

function fmt(n: number | null | undefined, d = 2) {
  if (n == null || isNaN(n)) return '--'
  return Number(n).toFixed(d)
}

// ---- intraday chart ----

const intradayOption = computed(() => {
  if (!intraday.value?.points?.length) return {}
  const pts = intraday.value.points
  const times = pts.map(p => p.time)
  const prices = pts.map(p => p.price)
  const avgs = pts.map(p => p.avgPrice)
  const vols = pts.map(p => p.volume)

  return {
    grid: [{ left: 60, right: 14, top: 10, height: '60%' }, { left: 60, right: 14, top: '78%', height: '14%' }],
    xAxis: [{ type: 'category' as const, data: times, gridIndex: 0, axisLabel: { show: false } },
             { type: 'category' as const, data: times, gridIndex: 1, axisLabel: { color: '#94a3b8', fontSize: 10 }}],
    yAxis: [{ type: 'value' as const, gridIndex: 0, scale: true, axisLabel: { color: '#94a3b8', fontSize: 10 }},
            { type: 'value' as const, gridIndex: 1, axisLabel: { show: false }}],
    series: [
      { type: 'line', data: prices, smooth: true, showSymbol: false, lineStyle: { color: '#60a5fa', width: 1.5 }, areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(96,165,250,0.25)' }, { offset: 1, color: 'rgba(96,165,250,0)' }] } } },
      { type: 'line', data: avgs, smooth: true, showSymbol: false, lineStyle: { color: '#fbbf24', width: 1, type: 'dashed' } },
      { type: 'bar', data: vols, xAxisIndex: 1, yAxisIndex: 1, itemStyle: { color: (p: { value: number }) => p.value >= 0 ? '#ef444466' : '#22c55e66' } },
    ],
    tooltip: { trigger: 'axis' as const },
  }
})

// ---- K-line chart ----

const klineOption = computed(() => {
  if (!kline.value?.points?.length) return {}
  const pts = kline.value.points
  const dates = pts.map(p => {
    const d = new Date(p.timestamp)
    return `${d.getMonth() + 1}/${d.getDate()}`
  })
  const ohlc = pts.map(p => [p.open, p.close, p.low, p.high])
  const vols = pts.map(p => p.volume)

  // MA5 / MA10 / MA20
  function ma(data: number[], n: number) {
    return data.map((_, i) => {
      if (i < n - 1) return null
      let sum = 0
      for (let j = i - n + 1; j <= i; j++) sum += data[j]
      return +(sum / n).toFixed(2)
    })
  }
  const closes = pts.map(p => p.close)
  const ma5 = ma(closes, 5)
  const ma10 = ma(closes, 10)
  const ma20 = ma(closes, 20)

  const upColor = '#ef4444'; const downColor = '#22c55e'

  return {
    grid: [{ left: 60, right: 14, top: 10, height: '60%' }, { left: 60, right: 14, top: '78%', height: '14%' }],
    xAxis: [{ type: 'category' as const, data: dates, gridIndex: 0, axisLabel: { show: false } },
             { type: 'category' as const, data: dates, gridIndex: 1, axisLabel: { color: '#94a3b8', fontSize: 10 }}],
    yAxis: [{ type: 'value' as const, gridIndex: 0, scale: true, axisLabel: { color: '#94a3b8', fontSize: 10 }},
            { type: 'value' as const, gridIndex: 1, axisLabel: { show: false }}],
    series: [
      { type: 'candlestick', data: ohlc, itemStyle: { color: upColor, color0: downColor, borderColor: upColor, borderColor0: downColor } },
      { type: 'line', data: ma5, smooth: true, showSymbol: false, lineStyle: { color: '#fbbf24', width: 1 } },
      { type: 'line', data: ma10, smooth: true, showSymbol: false, lineStyle: { color: '#a78bfa', width: 1 } },
      { type: 'line', data: ma20, smooth: true, showSymbol: false, lineStyle: { color: '#fb923c', width: 1 } },
      { type: 'bar', data: vols.map((v, i) => [i, v, (ohlc[i] as number[])[1] >= (ohlc[i] as number[])[0] ? 1 : -1]), xAxisIndex: 1, yAxisIndex: 1,
        itemStyle: { color: (p: { value: number[] }) => (p.value[2] as number) >= 0 ? upColor + '66' : downColor + '66' } },
    ],
    tooltip: { trigger: 'axis' as const },
    dataZoom: [{ type: 'inside' as const, xAxisIndex: [0, 1] }],
  }
})

// ---- capital flow pie ----

const flowOption = computed(() => {
  if (!capitalFlow.value) return {}
  const items: { name: string; value: number }[] = []
  if (capitalFlow.value.superLarge) items.push({ name: '主力', value: Math.abs(capitalFlow.value.superLarge) })
  if (capitalFlow.value.medium) items.push({ name: '中单', value: Math.abs(capitalFlow.value.medium) })
  if (capitalFlow.value.retail) items.push({ name: '散户', value: Math.abs(capitalFlow.value.retail) })
  if (!items.length) return {}
  return {
    title: { text: '资金流向', left: 'center', top: 0, textStyle: { color: 'var(--text-color)', fontSize: 13 } },
    tooltip: { trigger: 'item' as const },
    legend: { bottom: 0, textStyle: { color: 'var(--text-muted)', fontSize: 11 } },
    series: [{ type: 'pie', radius: ['40%', '70%'], data: items, label: { color: 'var(--text-muted)', fontSize: 11 },
      itemStyle: { borderRadius: 4 } }],
  }
})

// ---- data loading ----

let refreshTimer: ReturnType<typeof setInterval> | null = null

async function loadData() {
  try {
    const [q, id, kl, cf] = await Promise.all([
      fetchQuote(code.value).catch(() => null),
      fetchIntraday(code.value).catch(() => null),
      fetchKline(code.value, klinePeriod.value).catch(() => null),
      fetchCapitalFlow(code.value).catch(() => null),
    ])
    quote.value = q; intraday.value = id; kline.value = kl; capitalFlow.value = cf
  } catch { /* ignore */ }
  loading.value = false
}

watch(klinePeriod, () => { fetchKline(code.value, klinePeriod.value).then(d => { kline.value = d }) })

async function doTrade(mode: 'buy' | 'sell') {
  try {
    const fn = mode === 'buy' ? buyStock : sellStock
    await fn(code.value, tradeShares.value)
    ElMessage.success(mode === 'buy' ? '买入成功' : '卖出成功')
  } catch (e: unknown) { ElMessage.error(e instanceof Error ? e.message : '交易失败') }
}

onMounted(() => {
  loadData()
  refreshTimer = setInterval(async () => {
    try {
      const [q, id] = await Promise.all([
        fetchQuote(code.value).catch(() => null),
        fetchIntraday(code.value).catch(() => null),
      ])
      if (q) quote.value = q
      if (id) intraday.value = id
    } catch { /* ignore */ }
  }, 10000)
})

onUnmounted(() => { if (refreshTimer) clearInterval(refreshTimer) })
</script>

<template>
  <div class="stock-detail">
    <div class="detail-header">
      <button class="back-btn" @click="router.push('/tools/stock')">&larr; 返回</button>
      <h1 v-if="quote" class="stock-name">{{ quote.name }} <span class="stock-code">{{ quote.code }}</span></h1>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <template v-else-if="quote">
      <div class="quote-bar" :class="changeClass">
        <div class="quote-main">
          <span class="price">{{ fmt(quote.price, 3) }}</span>
          <span class="change">{{ (quote.changePct ?? 0) >= 0 ? '+' : '' }}{{ fmt(quote.change) }} ({{ (quote.changePct ?? 0) >= 0 ? '+' : '' }}{{ fmt(quote.changePct) }}%)</span>
        </div>
        <div class="quote-sub">
          <span>开 {{ fmt(quote.open) }}</span>
          <span>高 {{ fmt(quote.high) }}</span>
          <span>低 {{ fmt(quote.low) }}</span>
          <span>昨收 {{ fmt(quote.prevClose) }}</span>
          <span>量 {{ (quote.volume || 0).toLocaleString() }}</span>
        </div>
      </div>

      <!-- 分时图 -->
      <div class="chart-section">
        <h3>分时图</h3>
        <VChart v-if="intraday?.points?.length" :option="intradayOption" autoresize class="chart-box" />
        <p v-else class="no-data">暂无分时数据</p>
      </div>

      <!-- K线图 -->
      <div class="chart-section">
        <div class="chart-header">
          <h3>K线图</h3>
          <div class="period-tabs">
            <button :class="{ active: klinePeriod === 'day' }" @click="klinePeriod = 'day'">日K</button>
            <button :class="{ active: klinePeriod === 'week' }" @click="klinePeriod = 'week'">周K</button>
            <button :class="{ active: klinePeriod === 'month' }" @click="klinePeriod = 'month'">月K</button>
          </div>
        </div>
        <div class="ma-legend">
          <span style="color:#fbbf24">─ MA5</span>
          <span style="color:#a78bfa">─ MA10</span>
          <span style="color:#fb923c">─ MA20</span>
        </div>
        <VChart v-if="kline?.points?.length" :option="klineOption" autoresize class="chart-box" />
        <p v-else class="no-data">暂无K线数据</p>
      </div>

      <!-- 资金流向 + 交易 -->
      <div class="bottom-row">
        <div class="flow-section">
          <h3>资金流向</h3>
          <VChart v-if="Object.keys(flowOption).length" :option="flowOption" autoresize class="flow-chart" />
          <p v-else class="no-data">{{ capitalFlow?.note || '暂无数据' }}</p>
        </div>

        <div class="trade-section">
          <h3>快速交易</h3>
          <div class="trade-card">
            <div class="trade-row">
              <label>数量（股）</label>
              <input v-model.number="tradeShares" type="number" min="100" step="100" />
            </div>
            <div class="trade-row" v-if="quote">
              <label>预估金额</label>
              <span class="est">{{ fmt(quote.price * tradeShares) }} 元</span>
            </div>
            <div class="trade-btns">
              <button class="btn-buy" @click="doTrade('buy')">买入</button>
              <button class="btn-sell" @click="doTrade('sell')">卖出</button>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.stock-detail { max-width: 1100px; margin: 0 auto; padding: 20px 16px; color: var(--text-color); }

.detail-header { display: flex; align-items: center; gap: 16px; margin-bottom: 16px; }
.back-btn {
  background: var(--surface-2); border: 1px solid var(--glass-border);
  color: var(--text-muted); padding: 8px 14px; border-radius: 8px; cursor: pointer; font-size: 0.85rem;
}
.back-btn:hover { color: var(--primary-color); }
.stock-name { font-size: 1.2rem; margin: 0; }
.stock-code { color: var(--text-muted); font-size: 0.85rem; font-weight: 400; }

.loading, .no-data { text-align: center; padding: 40px; color: var(--text-muted); }

/* quote bar */
.quote-bar { padding: 16px 20px; border-radius: 14px; margin-bottom: 20px; background: var(--surface-2); border: 1px solid var(--glass-border); }
.quote-bar.up { border-left: 4px solid #ef4444; }
.quote-bar.down { border-left: 4px solid #22c55e; }
.quote-main { display: flex; align-items: baseline; gap: 14px; margin-bottom: 8px; }
.price { font-size: 2rem; font-weight: 800; }
.quote-bar.up .price, .quote-bar.up .change { color: #ef4444; }
.quote-bar.down .price, .quote-bar.down .change { color: #22c55e; }
.change { font-size: 1.1rem; font-weight: 600; }
.quote-sub { display: flex; gap: 18px; font-size: 0.82rem; color: var(--text-muted); }

/* charts */
.chart-section { margin-bottom: 24px; }
.chart-section h3 { font-size: 0.95rem; font-weight: 600; margin: 0 0 8px; color: var(--text-muted); }
.chart-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.chart-header h3 { margin: 0; }
.chart-box { width: 100%; height: 380px; background: var(--surface-1); border-radius: 12px; border: 1px solid var(--glass-border); }

.period-tabs { display: flex; gap: 4px; }
.period-tabs button {
  background: var(--surface-2); border: 1px solid var(--glass-border); color: var(--text-muted);
  padding: 5px 14px; border-radius: 6px; cursor: pointer; font-size: 0.8rem; transition: all 0.2s;
}
.period-tabs button.active { background: var(--primary-color); color: #fff; border-color: transparent; }

.ma-legend { display: flex; gap: 16px; font-size: 0.75rem; margin-bottom: 6px; }

/* bottom row */
.bottom-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.flow-section h3, .trade-section h3 { font-size: 0.95rem; font-weight: 600; margin: 0 0 8px; color: var(--text-muted); }
.flow-chart { width: 100%; height: 200px; background: var(--surface-1); border-radius: 12px; border: 1px solid var(--glass-border); }

.trade-card {
  background: var(--surface-1); border: 1px solid var(--glass-border); border-radius: 12px; padding: 16px;
}
.trade-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.trade-row label { font-size: 0.82rem; color: var(--text-muted); }
.trade-row input {
  width: 120px; padding: 8px 10px; border-radius: 6px;
  border: 1px solid var(--glass-border); background: var(--surface-2); color: var(--text-color); font-size: 0.9rem; outline: none;
}
.trade-row input:focus { border-color: var(--primary-color); }
.est { font-weight: 600; }
.trade-btns { display: flex; gap: 10px; }
.btn-buy, .btn-sell {
  flex: 1; padding: 10px; border-radius: 8px; border: none; font-size: 0.9rem; font-weight: 600; cursor: pointer; transition: all 0.2s;
}
.btn-buy { background: rgba(239, 68, 68, 0.15); color: #dc2626; border: 1px solid rgba(239, 68, 68, 0.3); }
.btn-buy:hover { background: rgba(239, 68, 68, 0.28); }
.btn-sell { background: rgba(34, 197, 94, 0.15); color: #16a34a; border: 1px solid rgba(34, 197, 94, 0.3); }
.btn-sell:hover { background: rgba(34, 197, 94, 0.28); }

@media (max-width: 768px) {
  .bottom-row { grid-template-columns: 1fr; }
  .quote-sub { flex-wrap: wrap; gap: 10px; }
  .price { font-size: 1.5rem; }
  .chart-box { height: 280px; }
}
</style>
