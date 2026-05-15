package com.mywebside.blog.stock;

import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.persistence.entity.UserEntity;
import com.mywebside.blog.persistence.mapper.UserEntityMapper;
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
  private final StockOrderRepository orderRepo;
  private final StockDataService dataService;
  private final UserEntityMapper userMapper;
  private final RestClient restClient;

  public StockService(StockPortfolioRepository portfolioRepo, StockTradeRepository tradeRepo,
                      StockOrderRepository orderRepo, StockDataService dataService,
                      UserEntityMapper userMapper, RestClient.Builder rb) {
    this.portfolioRepo = portfolioRepo;
    this.tradeRepo = tradeRepo;
    this.orderRepo = orderRepo;
    this.dataService = dataService;
    this.userMapper = userMapper;
    this.restClient = rb.build();
  }

  /** Fetch real-time quote. */
  public StockDataService.Quote fetchQuote(String rawCode) {
    return dataService.fetchQuote(normalizeCode(rawCode));
  }

  /** Search stocks by keyword (A-shares via Tencent, US/HK via Yahoo). Optional {@code market}: cn | us | hk | all. */
  public List<StockSearchResult> search(String keyword, String market) {
    if (keyword == null || keyword.isBlank()) return Collections.emptyList();
    List<StockSearchResult> results = new ArrayList<>();

    // A-share search via Tencent smartbox
    try {
      String body = restClient.get()
          .uri("https://smartbox.gtimg.cn/s3/?q=" + keyword.trim() + "&t=all&c=20")
          .header("Referer", "https://finance.qq.com").retrieve().body(String.class);
      if (body != null && !body.isBlank()) results.addAll(parseSearchResult(body));
    } catch (Exception e) { log.debug("Tencent search failed: {}", e.getMessage()); }

    // US/HK stock search via Yahoo
    try {
      String yahooBody = restClient.get()
          .uri("https://query1.finance.yahoo.com/v1/finance/search?q=" + keyword.trim() + "&quotesCount=12")
          .header("User-Agent", "Mozilla/5.0").retrieve().body(String.class);
      if (yahooBody != null && !yahooBody.isBlank()) results.addAll(parseYahooSearch(yahooBody));
    } catch (Exception e) { log.debug("Yahoo search failed: {}", e.getMessage()); }

    LinkedHashMap<String, StockSearchResult> dedup = new LinkedHashMap<>();
    for (StockSearchResult r : results) {
      String key = r.fullCode().toLowerCase(Locale.ROOT);
      dedup.putIfAbsent(key, r);
    }
    results = new ArrayList<>(dedup.values());

    String m = market == null || market.isBlank() ? "all" : market.toLowerCase(Locale.ROOT);
    if ("cn".equals(m)) {
      return results.stream().filter(StockService::isCnBoardSearchCode).limit(20).toList();
    }
    if ("us".equals(m)) {
      return results.stream().filter(r -> !isCnBoardSearchCode(r) && !isHkSearchCode(r)).limit(20).toList();
    }
    if ("hk".equals(m)) {
      return results.stream().filter(StockService::isHkSearchCode).limit(20).toList();
    }
    return results.stream().limit(25).toList();
  }

  private static boolean isCnBoardSearchCode(StockSearchResult r) {
    String f = r.fullCode().toLowerCase(Locale.ROOT);
    return f.startsWith("sh") || f.startsWith("sz");
  }

  private static boolean isHkSearchCode(StockSearchResult r) {
    String f = r.fullCode().toUpperCase(Locale.ROOT);
    return f.endsWith(".HK") || f.contains(".HK");
  }

  public PortfolioSummary getPortfolio(Long userId) {
    List<StockPortfolio> holdings = portfolioRepo.findByUserId(userId);
    BigDecimal totalCost = BigDecimal.ZERO;
    BigDecimal totalMarket = BigDecimal.ZERO;
    List<HoldingDto> items = new ArrayList<>();

    for (StockPortfolio h : holdings) {
      StockDataService.Quote q = dataService.fetchQuote(h.getStockCode());
      BigDecimal cost = h.getAvgCost().multiply(BigDecimal.valueOf(h.getShares()));
      totalCost = totalCost.add(cost);
      if (q.price().compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal market = q.price().multiply(BigDecimal.valueOf(h.getShares()));
        totalMarket = totalMarket.add(market);
        BigDecimal pnl = market.subtract(cost);
        BigDecimal pnlPct = pnl.divide(cost, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        items.add(new HoldingDto(h.getStockCode(), h.getStockName(), q.name(), h.getShares(),
            h.getAvgCost(), q.price(), pnl, pnlPct));
      } else {
        totalMarket = totalMarket.add(cost);
        items.add(new HoldingDto(h.getStockCode(), h.getStockName(),
            q.name().isEmpty() ? h.getStockName() : q.name(), h.getShares(), h.getAvgCost(), null,
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
        .map(e -> {
          String display = userMapper.findById(e.getKey())
              .map(StockService::displayNameForUser)
              .orElse("用户" + e.getKey());
          return new LeaderboardEntry(e.getKey(), display, e.getValue());
        })
        .toList();
  }

  private static String displayNameForUser(UserEntity u) {
    String nick = u.getNickname();
    if (nick != null && !nick.isBlank()) return nick.trim();
    return u.getUsername();
  }

  @Transactional
  public TradeResultDto buy(Long userId, String rawCode, int shares) {
    if (shares <= 0) throw new BusinessException(400, "买入股数必须大于0");
    String code = normalizeCode(rawCode);
    validateLotShares(code, shares);
    StockDataService.Quote q = dataService.fetchQuote(code);
    if (q.price().compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException(502, "获取股票行情失败");

    BigDecimal cost = q.price().multiply(BigDecimal.valueOf(shares));
    BigDecimal fee = cost.multiply(FEE_RATE).setScale(3, RoundingMode.HALF_UP);
    if (fee.compareTo(new BigDecimal("5")) < 0) fee = new BigDecimal("5");
    BigDecimal total = cost.add(fee);

    BigDecimal cash = getCash(userId);
    if (cash.compareTo(total) < 0) throw new BusinessException(400, "可用资金不足");

    StockPortfolio holding = portfolioRepo.findByUserIdAndStockCode(userId, code).orElse(null);
    if (holding != null) {
      BigDecimal oldTotal = holding.getAvgCost().multiply(BigDecimal.valueOf(holding.getShares()));
      int newShares = holding.getShares() + shares;
      BigDecimal newAvg = oldTotal.add(cost).divide(BigDecimal.valueOf(newShares), 3, RoundingMode.HALF_UP);
      holding.setShares(newShares);
      holding.setAvgCost(newAvg);
      holding.setStockName(q.name());
      holding.setUpdatedAt(Instant.now());
      portfolioRepo.save(holding);
    } else {
      holding = new StockPortfolio(userId, code, q.name(), shares, q.price());
      portfolioRepo.save(holding);
    }

    StockTrade trade = new StockTrade(userId, code, q.name(), StockTrade.TradeType.BUY, shares, q.price(), fee);
    tradeRepo.save(trade);

    BigDecimal remaining = cash.subtract(total);
    return new TradeResultDto("BUY", code, q.name(), shares, q.price(), fee, remaining, cost);
  }

  @Transactional
  public TradeResultDto sell(Long userId, String rawCode, int shares) {
    if (shares <= 0) throw new BusinessException(400, "卖出股数必须大于0");
    String code = normalizeCode(rawCode);
    validateLotShares(code, shares);
    StockPortfolio holding = portfolioRepo.findByUserIdAndStockCode(userId, code)
        .orElseThrow(() -> new BusinessException(400, "未持有该股票"));
    if (holding.getShares() < shares) throw new BusinessException(400, "持仓不足");

    StockDataService.Quote q = dataService.fetchQuote(code);
    if (q.price().compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException(502, "获取股票行情失败");

    BigDecimal revenue = q.price().multiply(BigDecimal.valueOf(shares));
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
    StockTrade trade = new StockTrade(userId, code, q.name(), StockTrade.TradeType.SELL, shares, q.price(), fee);
    trade.setProfitLoss(profitLoss);
    tradeRepo.save(trade);

    return new TradeResultDto("SELL", code, q.name(), shares, q.price(), fee, cash.add(total), revenue);
  }

  // ---- order management ----

  @Transactional
  public OrderDto placeOrder(Long userId, String rawCode, String type, String orderType,
                             BigDecimal limitPrice, int shares) {
    if (shares <= 0) throw new BusinessException(400, "委托数量必须大于0");
    String code = normalizeCode(rawCode);
    validateLotShares(code, shares);
    StockOrder.TradeType tt = "SELL".equalsIgnoreCase(type) ? StockOrder.TradeType.SELL : StockOrder.TradeType.BUY;
    StockOrder.OrderType ot = "MARKET".equalsIgnoreCase(orderType) ? StockOrder.OrderType.MARKET : StockOrder.OrderType.LIMIT;

    if (ot == StockOrder.OrderType.LIMIT && (limitPrice == null || limitPrice.compareTo(BigDecimal.ZERO) <= 0)) {
      throw new BusinessException(400, "请输入有效的限价");
    }

    StockDataService.Quote q = dataService.fetchQuote(code);
    if (q.price().compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException(502, "获取行情失败");
    BigDecimal price = ot == StockOrder.OrderType.MARKET ? q.price() : limitPrice;

    if (tt == StockOrder.TradeType.BUY) {
      BigDecimal cost = price.multiply(BigDecimal.valueOf(shares));
      BigDecimal fee = cost.multiply(FEE_RATE).setScale(3, RoundingMode.HALF_UP);
      if (fee.compareTo(new BigDecimal("5")) < 0) fee = new BigDecimal("5");
      BigDecimal cash = getCash(userId);
      if (cash.compareTo(cost.add(fee)) < 0) throw new BusinessException(400, "可用资金不足");
    } else {
      StockPortfolio holding = portfolioRepo.findByUserIdAndStockCode(userId, code)
          .orElseThrow(() -> new BusinessException(400, "未持有该股票"));
      if (holding.getShares() < shares) throw new BusinessException(400, "持仓不足");
    }

    if (ot == StockOrder.OrderType.MARKET) {
      if (tt == StockOrder.TradeType.BUY) return toOrderDto(buy(userId, rawCode, shares), q);
      else return toOrderDto(sell(userId, rawCode, shares), q);
    }

    StockOrder order = new StockOrder(userId, code, q.name(), tt, ot, price, shares);
    orderRepo.save(order);
    return OrderDto.from(order);
  }

  @Transactional
  public OrderDto cancelOrder(Long userId, Long orderId) {
    StockOrder order = orderRepo.findById(orderId)
        .orElseThrow(() -> new BusinessException(404, "委托单不存在"));
    if (!order.getUserId().equals(userId)) throw new BusinessException(403, "无权操作");
    if (order.getStatus() != StockOrder.OrderStatus.PENDING) throw new BusinessException(400, "仅可撤销待成交委托");
    order.setStatus(StockOrder.OrderStatus.CANCELLED);
    order.setUpdatedAt(Instant.now());
    orderRepo.save(order);
    return OrderDto.from(order);
  }

  public List<OrderDto> getOrders(Long userId) {
    return orderRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(OrderDto::from).toList();
  }

  /** Scheduled: try to match pending limit orders against current market prices. */
  @Transactional
  public int matchOrders() {
    List<StockOrder> pending = orderRepo.findByStatusInOrderByCreatedAtAsc(
        java.util.List.of(StockOrder.OrderStatus.PENDING));
    int matched = 0;
    for (StockOrder o : pending) {
      try {
        StockDataService.Quote q = dataService.fetchQuote(o.getStockCode());
        if (q.price().compareTo(BigDecimal.ZERO) <= 0) continue;
        boolean shouldFill = false;
        if (o.getType() == StockOrder.TradeType.BUY && q.price().compareTo(o.getPrice()) <= 0) shouldFill = true;
        if (o.getType() == StockOrder.TradeType.SELL && q.price().compareTo(o.getPrice()) >= 0) shouldFill = true;
        if (shouldFill) {
          if (o.getType() == StockOrder.TradeType.BUY) buy(o.getUserId(), o.getStockCode(), o.getShares());
          else sell(o.getUserId(), o.getStockCode(), o.getShares());
          o.setStatus(StockOrder.OrderStatus.FILLED);
          o.setFilledShares(o.getShares());
          o.setUpdatedAt(Instant.now());
          orderRepo.save(o);
          matched++;
        }
      } catch (Exception e) { log.debug("Order {} match failed: {}", o.getId(), e.getMessage()); }
    }
    return matched;
  }

  private OrderDto toOrderDto(TradeResultDto t, StockDataService.Quote q) {
    return new OrderDto(null, q.code(), q.name(), t.type(), "MARKET",
        t.price(), t.shares(), t.shares(), "FILLED", java.time.Instant.now(), java.time.Instant.now());
  }

  public record OrderDto(Long id, String code, String name, String type, String orderType,
      BigDecimal price, int shares, int filledShares, String status,
      Instant createdAt, Instant updatedAt) {
    public static OrderDto from(StockOrder o) {
      return new OrderDto(o.getId(), o.getStockCode(), o.getStockName(), o.getType().name(),
          o.getOrderType().name(), o.getPrice(), o.getShares(), o.getFilledShares(),
          o.getStatus().name(), o.getCreatedAt(), o.getUpdatedAt());
    }
  }

  /** A 股沪深主板/创业板等常见代码：委托数量须为 100 股的整数倍（与真实交易习惯一致）。 */
  static void validateLotShares(String normalizedCode, int shares) {
    String c = normalizedCode.toLowerCase(Locale.ROOT);
    if (!isCnSixDigitBoardCode(c)) return;
    if (shares < 100 || shares % 100 != 0) {
      throw new BusinessException(400, "A股委托数量须为100股的整数倍");
    }
  }

  static boolean isCnSixDigitBoardCode(String c) {
    if (!(c.startsWith("sh") || c.startsWith("sz"))) return false;
    if (c.length() != 8) return false;
    for (int i = 2; i < 8; i++) {
      if (!Character.isDigit(c.charAt(i))) return false;
    }
    return true;
  }

  private BigDecimal getCash(Long userId) {
    List<StockTrade> trades = tradeRepo.findByUserIdOrderByTradedAtDesc(userId);
    if (trades.isEmpty()) return INITIAL_CASH;
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

  /**
   * 统一证券代码：沪深 sh/sz +6 位；港股 xxxx.hk（Yahoo）；美股等保持小写 ticker。
   * 注意：纯数字须先识别 6 位 A 股，再识别 1～5 位港股，避免「00700」被误判为深市。
   */
  static String normalizeCode(String raw) {
    String s = raw.trim().toLowerCase(Locale.ROOT);
    if (s.startsWith("sh") || s.startsWith("sz")) return s;
    if (s.endsWith(".hk")) return s;
    if (s.matches("\\d{6}")) {
      if (s.charAt(0) == '6') return "sh" + s;
      if (s.charAt(0) == '0' || s.charAt(0) == '3' || s.charAt(0) == '2') return "sz" + s;
      return s;
    }
    if (s.matches("\\d{1,5}")) {
      int v = Integer.parseInt(s);
      if (v > 9999) return v + ".hk";
      return String.format("%04d", v) + ".hk";
    }
    if (s.startsWith("6")) return "sh" + s;
    if (s.startsWith("0") || s.startsWith("3") || s.startsWith("2")) return "sz" + s;
    return s;
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

  static List<StockSearchResult> parseYahooSearch(String body) {
    List<StockSearchResult> list = new ArrayList<>();
    try {
      com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
      com.fasterxml.jackson.databind.JsonNode root = om.readTree(body);
      com.fasterxml.jackson.databind.JsonNode quotes = root.path("quotes");
      if (!quotes.isArray()) return list;
      for (com.fasterxml.jackson.databind.JsonNode q : quotes) {
        String symbol = q.path("symbol").asText();
        String name = q.path("shortname").asText();
        String type = q.path("quoteType").asText();
        if (symbol.isBlank() || name.isBlank()) continue;
        if (!"EQUITY".equals(type) && !"ETF".equals(type)) continue;
        list.add(new StockSearchResult(symbol, symbol, name));
        if (list.size() >= 10) break;
      }
    } catch (Exception e) { log.debug("Yahoo search parse failed"); }
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
      BigDecimal price, BigDecimal fee, BigDecimal cashAfter, BigDecimal grossAmount) {}
  public record TradeHistoryDto(String code, String name, String type, int shares,
      BigDecimal price, BigDecimal fee, BigDecimal profitLoss, Instant time) {}
  public record LeaderboardEntry(Long userId, String displayName, BigDecimal totalPnl) {}
}
