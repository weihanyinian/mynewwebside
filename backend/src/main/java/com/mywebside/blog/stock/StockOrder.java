package com.mywebside.blog.stock;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stock_orders")
public class StockOrder {
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

  @Enumerated(EnumType.STRING)
  @Column(name = "order_type", nullable = false, length = 6)
  private OrderType orderType = OrderType.LIMIT;

  @Column(nullable = false, precision = 10, scale = 3)
  private BigDecimal price;

  @Column(nullable = false)
  private int shares;

  @Column(name = "filled_shares")
  private int filledShares;

  @Enumerated(EnumType.STRING)
  @Column(length = 10)
  private OrderStatus status = OrderStatus.PENDING;

  @Column(name = "created_at")
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at")
  private Instant updatedAt = Instant.now();

  public StockOrder() {}

  public StockOrder(Long userId, String stockCode, String stockName, TradeType type,
                    OrderType orderType, BigDecimal price, int shares) {
    this.userId = userId; this.stockCode = stockCode; this.stockName = stockName;
    this.type = type; this.orderType = orderType; this.price = price; this.shares = shares;
  }

  public Long getId() { return id; }
  public Long getUserId() { return userId; }
  public String getStockCode() { return stockCode; }
  public String getStockName() { return stockName; }
  public void setStockName(String v) { this.stockName = v; }
  public TradeType getType() { return type; }
  public OrderType getOrderType() { return orderType; }
  public BigDecimal getPrice() { return price; }
  public int getShares() { return shares; }
  public int getFilledShares() { return filledShares; }
  public void setFilledShares(int v) { this.filledShares = v; }
  public OrderStatus getStatus() { return status; }
  public void setStatus(OrderStatus v) { this.status = v; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant v) { this.updatedAt = v; }

  public enum TradeType { BUY, SELL }
  public enum OrderType { LIMIT, MARKET }
  public enum OrderStatus { PENDING, PARTIAL, FILLED, CANCELLED }
}
