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
  private final UserEntityMapper userMapper;

  public StockController(StockService stockService, UserEntityMapper userMapper) {
    this.stockService = stockService;
    this.userMapper = userMapper;
  }

  @GetMapping("/quote")
  public ApiResponse<StockService.StockQuote> quote(@RequestParam String code) {
    StockService.StockQuote q = stockService.fetchQuote(code);
    if (q == null) return ApiResponse.error(502, "获取行情失败");
    return ApiResponse.ok(q);
  }

  @GetMapping("/search")
  public ApiResponse<List<StockService.StockSearchResult>> search(@RequestParam String keyword) {
    return ApiResponse.ok(stockService.search(keyword));
  }

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
        .orElseThrow(() -> new BusinessException(401, "用户不存在"))
        .getId();
  }

  public record TradeRequest(String code, int shares) {}
}
