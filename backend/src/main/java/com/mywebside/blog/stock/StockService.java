package com.mywebside.blog.stock;

import com.mywebside.blog.common.BusinessException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
public class StockService {
  private static final Logger log = LoggerFactory.getLogger(StockService.class);
  private static final BigDecimal INITIAL_CASH = new BigDecimal("1000000");
  private static final BigDecimal FEE_RATE = new BigDecimal("0.0003");

  private final StockPortfolioRepository portfolioRepo;
  private final StockTradeRepository tradeRepo;
  private final RestClient restClient;

  public StockService(StockPortfolioRepository portfolioRepo, StockTradeRepository tradeRepo, RestClient.Builder rb) {
    this.portfolioRepo = portfolioRepo;
    this.tradeRepo = tradeRepo;
    this.restClient = rb.build();
  }

  /** Fetch real-time quote from Sina. Returns null if unavailable. */
  public StockQuote fetchQuote(String rawCode) {
    String code = normalizeCode(rawCode);
    String sinaCode = toSinaCode(code);
    try {
      String body = restClient.get()
          .uri("https://hq.sinajs.cn/list=" + sinaCode)
          .header("Referer", "https://finance.sina.com.cn")
          .retrieve().body(String.class);
      if (body == null || body.isBlank() || body.contains("\"\"")) return null;
      return parseSina(body, code);
    } catch (Exception e) {
      log.warn("Failed to fetch quote for {}", code, e);
      return null;
    }
  }

  /** Search stocks by keyword via Tencent smartbox. */
  public List<StockSearchResult> search(String keyword) {
    if (keyword == null || keyword.isBlank()) return Collections.emptyList();
    try {
      String body = restClient.get()
          .uri("https://smartbox.gtimg.cn/s3/?q=" + keyword.trim() + "&t=all&c=30")
          .header("Referer", "https://finance.qq.com")
          .retrieve().body(String.class);
      if (body == null || body.isBlank()) return Collections.emptyList();
      return parseSearchResult(body);
    } catch (Exception e) {
      log.warn("Search failed for keyword={}", keyword, e);
      return Collections.emptyList();
    }
  }

  public PortfolioSummary getPortfolio(Long userId) {
    List<StockPortfolio> holdings = portfolioRepo.findByUserId(userId);
    BigDecimal totalCost = BigDecimal.ZERO;
    BigDecimal totalMarket = BigDecimal.ZERO;
    List<HoldingDto> items = new ArrayList<>();

    for (StockPortfolio h : holdings) {
      StockQuote q = fetchQuote(h.getStockCode());
      BigDecimal cost = h.getAvgCost().multiply(BigDecimal.valueOf(h.getShares()));
      totalCost = totalCost.add(cost);
      if (q != null && q.price.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal market = q.price.multiply(BigDecimal.valueOf(h.getShares()));
        totalMarket = totalMarket.add(market);
        BigDecimal pnl = market.subtract(cost);
        BigDecimal pnlPct = pnl.divide(cost, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        items.add(new HoldingDto(h.getStockCode(), h.getStockName(), q.name, h.getShares(),
            h.getAvgCost(), q.price, pnl, pnlPct));
      } else {
        totalMarket = totalMarket.add(cost);
        items.add(new HoldingDto(h.getStockCode(), h.getStockName(),
            q != null ? q.name : h.getStockName(), h.getShares(), h.getAvgCost(), null,
            BigDecimal.ZERO, BigDecimal.ZERO));
      }
    }

    BigDecimal cash = getCash(userId);
    BigDecimal totalAssets = cash.add(totalMarket);
    BigDecimal totalPnl = totalAssets.subtract(INITIAL_CASH);
    BigDecimal totalPnlPct = totalPnl.divide(INITIAL_CASH, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));

    return new PortfolioSummary(cash, totalMarket, totalAssets, totalPnl, totalPnlPct, items);
  }

  public List<TradeHistoryDto> getTradeHistory(Long userId) {
    List<StockTrade> trades = tradeRepo.findByUserIdOrderByTradedAtDesc(userId);
    return trades.stream().map(t -> new TradeHistoryDto(
        t.getStockCode(), t.getStockName(), t.getType().name(), t.getShares(),
        t.getPrice(), t.getFee(), t.getProfitLoss(), t.getTradedAt()
    )).toList();
  }

  public List<LeaderboardEntry> getLeaderboard() {
    Map<Long, BigDecimal> pnlByUser = new HashMap<>();
    for (StockTrade t : tradeRepo.findAll()) {
      if (t.getProfitLoss() != null) {
        pnlByUser.merge(t.getUserId(), t.getProfitLoss(), BigDecimal::add);
      }
    }
    return pnlByUser.entrySet().stream()
        .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
        .limit(20)
        .map(e -> new LeaderboardEntry(e.getKey(), e.getValue()))
        .toList();
  }

  @Transactional
  public TradeResultDto buy(Long userId, String rawCode, int shares) {
    if (shares <= 0) throw new BusinessException(400, "买入股数必须大于0");
    StockQuote q = fetchQuote(rawCode);
    if (q == null) throw new BusinessException(502, "获取股票行情失败，请稍后再试");
    if (q.price.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException(400, "该股票暂不支持交易");

    BigDecimal cost = q.price.multiply(BigDecimal.valueOf(shares));
    BigDecimal fee = cost.multiply(FEE_RATE).setScale(3, RoundingMode.HALF_UP);
    if (fee.compareTo(new BigDecimal("5")) < 0) fee = new BigDecimal("5");
    BigDecimal total = cost.add(fee);

    BigDecimal cash = getCash(userId);
    if (cash.compareTo(total) < 0) throw new BusinessException(400, "可用资金不足");

    String code = normalizeCode(rawCode);
    StockPortfolio holding = portfolioRepo.findByUserIdAndStockCode(userId, code).orElse(null);
    if (holding != null) {
      BigDecimal oldTotal = holding.getAvgCost().multiply(BigDecimal.valueOf(holding.getShares()));
      int newShares = holding.getShares() + shares;
      BigDecimal newAvg = oldTotal.add(cost).divide(BigDecimal.valueOf(newShares), 3, RoundingMode.HALF_UP);
      holding.setShares(newShares);
      holding.setAvgCost(newAvg);
      holding.setStockName(q.name);
      holding.setUpdatedAt(Instant.now());
      portfolioRepo.save(holding);
    } else {
      holding = new StockPortfolio(userId, code, q.name, shares, q.price);
      portfolioRepo.save(holding);
    }

    StockTrade trade = new StockTrade(userId, code, q.name, StockTrade.TradeType.BUY, shares, q.price, fee);
    tradeRepo.save(trade);

    BigDecimal remaining = cash.subtract(total);
    return new TradeResultDto("BUY", code, q.name, shares, q.price, fee, remaining);
  }

  @Transactional
  public TradeResultDto sell(Long userId, String rawCode, int shares) {
    if (shares <= 0) throw new BusinessException(400, "卖出股数必须大于0");
    String code = normalizeCode(rawCode);
    StockPortfolio holding = portfolioRepo.findByUserIdAndStockCode(userId, code)
        .orElseThrow(() -> new BusinessException(400, "未持有该股票"));
    if (holding.getShares() < shares) throw new BusinessException(400, "持仓不足");

    StockQuote q = fetchQuote(code);
    if (q == null) throw new BusinessException(502, "获取股票行情失败");
    if (q.price.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException(400, "该股票暂不支持交易");

    BigDecimal revenue = q.price.multiply(BigDecimal.valueOf(shares));
    BigDecimal fee = revenue.multiply(FEE_RATE).setScale(3, RoundingMode.HALF_UP);
    if (fee.compareTo(new BigDecimal("5")) < 0) fee = new BigDecimal("5");
    BigDecimal total = revenue.subtract(fee);
    BigDecimal costBasis = holding.getAvgCost().multiply(BigDecimal.valueOf(shares));
    BigDecimal profitLoss = total.subtract(costBasis).setScale(3, RoundingMode.HALF_UP);

    int remaining = holding.getShares() - shares;
    if (remaining == 0) {
      portfolioRepo.delete(holding);
    } else {
      holding.setShares(remaining);
      holding.setUpdatedAt(Instant.now());
      portfolioRepo.save(holding);
    }

    BigDecimal cash = getCash(userId);
    StockTrade trade = new StockTrade(userId, code, q.name, StockTrade.TradeType.SELL, shares, q.price, fee);
    trade.setProfitLoss(profitLoss);
    tradeRepo.save(trade);

    return new TradeResultDto("SELL", code, q.name, shares, q.price, fee, cash.add(total));
  }

  private BigDecimal getCash(Long userId) {
    long count = tradeRepo.findByUserIdOrderByTradedAtDesc(userId).size();
    if (count == 0) return INITIAL_CASH;
    List<StockTrade> trades = tradeRepo.findByUserIdOrderByTradedAtDesc(userId);
    BigDecimal cash = INITIAL_CASH;
    for (StockTrade t : trades) {
      if (t.getType() == StockTrade.TradeType.BUY) {
        cash = cash.subtract(t.getPrice().multiply(BigDecimal.valueOf(t.getShares())).add(t.getFee()));
      } else {
        cash = cash.add(t.getPrice().multiply(BigDecimal.valueOf(t.getShares())).subtract(t.getFee()));
      }
    }
    return cash;
  }

  static String normalizeCode(String raw) {
    String s = raw.trim().toLowerCase();
    if (s.startsWith("sh") || s.startsWith("sz")) return s;
    if (s.startsWith("6")) return "sh" + s;
    if (s.startsWith("0") || s.startsWith("3") || s.startsWith("2")) return "sz" + s;
    return s;
  }

  static String toSinaCode(String normalized) { return normalized; }

  static StockQuote parseSina(String body, String code) {
    int start = body.indexOf("\"");
    int end = body.lastIndexOf("\"");
    if (start < 0 || end <= start) return null;
    String[] parts = body.substring(start + 1, end).split(",");
    if (parts.length < 4) return null;
    return new StockQuote(code, parts[0], new BigDecimal(parts[3]));
  }

  static List<StockSearchResult> parseSearchResult(String body) {
    List<StockSearchResult> list = new ArrayList<>();
    String[] items = body.split("\\^");
    for (String item : items) {
      if (item.isBlank()) continue;
      String[] f = item.split("~");
      if (f.length < 4) continue;
      String market = f[1];
      String code = f[2];
      String name = f[3];
      if ("GP-A".equals(market) || "GP-A-CYB".equals(market) || "GP-A-KCB".equals(market)) {
        String prefix = code.startsWith("6") ? "sh" : "sz";
        list.add(new StockSearchResult(prefix + code, code, name));
      } else if ("ETF".equals(market)) {
        list.add(new StockSearchResult(code, code, name));
      }
      if (list.size() >= 10) break;
    }
    return list;
  }

  // ---- DTO records ----
  public record StockQuote(String code, String name, BigDecimal price) {}
  public record StockSearchResult(String fullCode, String code, String name) {}
  public record HoldingDto(String code, String name, String realName, int shares,
      BigDecimal avgCost, BigDecimal currentPrice, BigDecimal pnl, BigDecimal pnlPct) {}
  public record PortfolioSummary(BigDecimal cash, BigDecimal marketValue, BigDecimal totalAssets,
      BigDecimal totalPnl, BigDecimal totalPnlPct, List<HoldingDto> holdings) {}
  public record TradeResultDto(String type, String code, String name, int shares,
      BigDecimal price, BigDecimal fee, BigDecimal cashAfter) {}
  public record TradeHistoryDto(String code, String name, String type, int shares,
      BigDecimal price, BigDecimal fee, BigDecimal profitLoss, Instant time) {}
  public record LeaderboardEntry(Long userId, BigDecimal totalPnl) {}
}
