package com.mywebside.blog.stock;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.persistence.mapper.UserEntityMapper;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
public class StockController {
  private final StockService stockService;
  private final StockDataService dataService;
  private final UserEntityMapper userMapper;

  public StockController(StockService stockService, StockDataService dataService, UserEntityMapper userMapper) {
    this.stockService = stockService;
    this.dataService = dataService;
    this.userMapper = userMapper;
  }

  // ---- real-time data ----

  @GetMapping("/quote")
  public ApiResponse<StockDataService.Quote> quote(@RequestParam String code) {
    StockDataService.Quote q = dataService.fetchQuote(code);
    if (q.price().compareTo(java.math.BigDecimal.ZERO) <= 0) return ApiResponse.error(502, "获取行情失败");
    return ApiResponse.ok(q);
  }

  /** Batch quotes for portfolio refresh. */
  @PostMapping("/quotes")
  public ApiResponse<List<StockDataService.Quote>> quotes(@RequestBody List<String> codes) {
    return ApiResponse.ok(dataService.fetchQuotes(codes));
  }

  @GetMapping("/intraday")
  public ApiResponse<StockDataService.IntradayData> intraday(@RequestParam String code) {
    return ApiResponse.ok(dataService.getIntraday(code));
  }

  @GetMapping("/kline")
  public ApiResponse<StockDataService.KlineData> kline(
      @RequestParam String code, @RequestParam(defaultValue = "day") String period) {
    return ApiResponse.ok(dataService.getKline(code, period));
  }

  @GetMapping("/capital-flow")
  public ApiResponse<StockDataService.CapitalFlow> capitalFlow(@RequestParam String code) {
    return ApiResponse.ok(dataService.getCapitalFlow(code));
  }

  // ---- hot stocks ----

  @GetMapping("/hot")
  public ApiResponse<List<StockDataService.HotStock>> hot(@RequestParam(defaultValue = "cn") String market) {
    return ApiResponse.ok(dataService.getHotStocks(market));
  }

  // ---- search ----

  @GetMapping("/search")
  public ApiResponse<List<StockService.StockSearchResult>> search(@RequestParam String keyword) {
    return ApiResponse.ok(stockService.search(keyword));
  }

  // ---- portfolio & trading ----

  @GetMapping("/portfolio")
  public ApiResponse<StockService.PortfolioSummary> portfolio(Principal principal) {
    return ApiResponse.ok(stockService.getPortfolio(resolveUserId(principal)));
  }

  @GetMapping("/trades")
  public ApiResponse<List<StockService.TradeHistoryDto>> trades(Principal principal) {
    return ApiResponse.ok(stockService.getTradeHistory(resolveUserId(principal)));
  }

  @GetMapping("/leaderboard")
  public ApiResponse<List<StockService.LeaderboardEntry>> leaderboard() {
    return ApiResponse.ok(stockService.getLeaderboard());
  }

  @PostMapping("/buy")
  public ApiResponse<StockService.TradeResultDto> buy(Principal principal, @RequestBody TradeRequest req) {
    return ApiResponse.ok(stockService.buy(resolveUserId(principal), req.code(), req.shares()));
  }

  @PostMapping("/sell")
  public ApiResponse<StockService.TradeResultDto> sell(Principal principal, @RequestBody TradeRequest req) {
    return ApiResponse.ok(stockService.sell(resolveUserId(principal), req.code(), req.shares()));
  }

  private Long resolveUserId(Principal principal) {
    if (principal == null) throw new BusinessException(401, "请先登录");
    return userMapper.findByUsername(principal.getName())
        .orElseThrow(() -> new BusinessException(401, "用户不存在")).getId();
  }

  // ---- order management ----

  @PostMapping("/order")
  public ApiResponse<StockService.OrderDto> placeOrder(Principal principal, @RequestBody OrderRequest req) {
    return ApiResponse.ok(stockService.placeOrder(resolveUserId(principal),
        req.code(), req.type(), req.orderType(), req.limitPrice(), req.shares()));
  }

  @DeleteMapping("/order/{id}")
  public ApiResponse<StockService.OrderDto> cancelOrder(Principal principal, @PathVariable Long id) {
    return ApiResponse.ok(stockService.cancelOrder(resolveUserId(principal), id));
  }

  @GetMapping("/orders")
  public ApiResponse<List<StockService.OrderDto>> orders(Principal principal) {
    return ApiResponse.ok(stockService.getOrders(resolveUserId(principal)));
  }

  public record TradeRequest(String code, int shares) {}
  public record OrderRequest(String code, String type, String orderType, java.math.BigDecimal limitPrice, int shares) {
    public OrderRequest {
      if (orderType == null || orderType.isBlank()) orderType = "LIMIT";
      if (limitPrice == null) limitPrice = java.math.BigDecimal.ZERO;
    }
  }
}
