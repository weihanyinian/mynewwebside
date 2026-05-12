package com.mywebside.blog.stock;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "stock_portfolio")
public class StockPortfolio {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "stock_code", nullable = false, length = 20)
  private String stockCode;

  @Column(name = "stock_name", length = 50)
  private String stockName;

  @Column(nullable = false)
  private int shares;

  @Column(name = "avg_cost", nullable = false, precision = 10, scale = 3)
  private java.math.BigDecimal avgCost;

  @Column(name = "updated_at")
  private Instant updatedAt = Instant.now();

  public StockPortfolio() {}

  public StockPortfolio(Long userId, String stockCode, String stockName, int shares, java.math.BigDecimal avgCost) {
    this.userId = userId;
    this.stockCode = stockCode;
    this.stockName = stockName;
    this.shares = shares;
    this.avgCost = avgCost;
  }

  public Long getId() { return id; }
  public Long getUserId() { return userId; }
  public String getStockCode() { return stockCode; }
  public String getStockName() { return stockName; }
  public void setStockName(String v) { this.stockName = v; }
  public int getShares() { return shares; }
  public void setShares(int v) { this.shares = v; }
  public java.math.BigDecimal getAvgCost() { return avgCost; }
  public void setAvgCost(java.math.BigDecimal v) { this.avgCost = v; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant v) { this.updatedAt = v; }
}
