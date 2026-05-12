package com.mywebside.blog.stock;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stock_trade")
public class StockTrade {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "stock_code", nullable = false, length = 20)
  private String stockCode;

  @Column(name = "stock_name", length = 50)
  private String stockName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 4)
  private TradeType type;

  @Column(nullable = false)
  private int shares;

  @Column(nullable = false, precision = 10, scale = 3)
  private BigDecimal price;

  @Column(precision = 10, scale = 3)
  private BigDecimal fee = BigDecimal.ZERO;

  @Column(name = "profit_loss", precision = 10, scale = 3)
  private BigDecimal profitLoss = BigDecimal.ZERO;

  @Column(name = "traded_at")
  private Instant tradedAt = Instant.now();

  public StockTrade() {}

  public StockTrade(Long userId, String stockCode, String stockName, TradeType type, int shares, BigDecimal price, BigDecimal fee) {
    this.userId = userId;
    this.stockCode = stockCode;
    this.stockName = stockName;
    this.type = type;
    this.shares = shares;
    this.price = price;
    this.fee = fee;
  }

  public Long getId() { return id; }
  public Long getUserId() { return userId; }
  public String getStockCode() { return stockCode; }
  public String getStockName() { return stockName; }
  public TradeType getType() { return type; }
  public int getShares() { return shares; }
  public BigDecimal getPrice() { return price; }
  public BigDecimal getFee() { return fee; }
  public BigDecimal getProfitLoss() { return profitLoss; }
  public void setProfitLoss(BigDecimal v) { this.profitLoss = v; }
  public Instant getTradedAt() { return tradedAt; }

  public enum TradeType { BUY, SELL }
}
