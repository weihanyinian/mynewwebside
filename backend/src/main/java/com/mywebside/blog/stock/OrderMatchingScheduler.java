package com.mywebside.blog.stock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderMatchingScheduler {
  private static final Logger log = LoggerFactory.getLogger(OrderMatchingScheduler.class);
  private final StockService stockService;

  public OrderMatchingScheduler(StockService stockService) {
    this.stockService = stockService;
  }

  @Scheduled(fixedRate = 30000)
  public void match() {
    try {
      int matched = stockService.matchOrders();
      if (matched > 0) log.info("Matched {} orders", matched);
    } catch (Exception e) { log.warn("Order matching failed", e); }
  }
}
