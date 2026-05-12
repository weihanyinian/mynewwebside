import { http, type ApiResponse } from './http'

// ---- types ----

export interface StockQuote {
  code: string; name: string; price: number; prevClose: number
  open: number; high: number; low: number; volume: number
  change?: number; changePct?: number
}

export interface StockSearchResult {
  fullCode: string; code: string; name: string
}

export interface HoldingItem {
  code: string; name: string; realName: string; shares: number
  avgCost: number; currentPrice: number | null; pnl: number; pnlPct: number
}

export interface PortfolioSummary {
  cash: number; marketValue: number; totalAssets: number
  totalPnl: number; totalPnlPct: number; holdings: HoldingItem[]
}

export interface TradeResult {
  type: string; code: string; name: string; shares: number
  price: number; fee: number; cashAfter: number
}

export interface TradeHistoryItem {
  code: string; name: string; type: string; shares: number
  price: number; fee: number; profitLoss: number; time: string
}

export interface LeaderboardEntry { userId: number; totalPnl: number }

// ---- intraday / k-line ----

export interface IntraPoint {
  time: string; price: number; volume: number; avgPrice: number
}

export interface IntradayData {
  preClose: number; points: IntraPoint[]
}

export interface KlinePoint {
  timestamp: number; open: number; close: number; high: number; low: number; volume: number
}

export interface KlineData {
  symbol: string; points: KlinePoint[]
}

export interface CapitalFlow {
  superLarge: number | null; medium: number | null; retail: number | null
  small: number | null; note: string | null
}

// ---- API functions ----

export async function fetchQuote(code: string): Promise<StockQuote> {
  const resp = await http.get<ApiResponse<StockQuote>>('/api/stock/quote', { params: { code } })
  return resp.data.data
}

export async function fetchQuotes(codes: string[]): Promise<StockQuote[]> {
  const resp = await http.post<ApiResponse<StockQuote[]>>('/api/stock/quotes', codes)
  return resp.data.data
}

export async function fetchIntraday(code: string): Promise<IntradayData> {
  const resp = await http.get<ApiResponse<IntradayData>>('/api/stock/intraday', { params: { code } })
  return resp.data.data
}

export async function fetchKline(code: string, period = 'day'): Promise<KlineData> {
  const resp = await http.get<ApiResponse<KlineData>>('/api/stock/kline', { params: { code, period } })
  return resp.data.data
}

export async function fetchCapitalFlow(code: string): Promise<CapitalFlow> {
  const resp = await http.get<ApiResponse<CapitalFlow>>('/api/stock/capital-flow', { params: { code } })
  return resp.data.data
}

export async function searchStocks(keyword: string) {
  const resp = await http.get<ApiResponse<StockSearchResult[]>>('/api/stock/search', { params: { keyword } })
  return resp.data.data
}

export async function fetchPortfolio() {
  const resp = await http.get<ApiResponse<PortfolioSummary>>('/api/stock/portfolio')
  return resp.data.data
}

export async function fetchTrades() {
  const resp = await http.get<ApiResponse<TradeHistoryItem[]>>('/api/stock/trades')
  return resp.data.data
}

export async function fetchLeaderboard() {
  const resp = await http.get<ApiResponse<LeaderboardEntry[]>>('/api/stock/leaderboard')
  return resp.data.data
}

export async function buyStock(code: string, shares: number) {
  const resp = await http.post<ApiResponse<TradeResult>>('/api/stock/buy', { code, shares })
  return resp.data.data
}

export async function sellStock(code: string, shares: number) {
  const resp = await http.post<ApiResponse<TradeResult>>('/api/stock/sell', { code, shares })
  return resp.data.data
}
