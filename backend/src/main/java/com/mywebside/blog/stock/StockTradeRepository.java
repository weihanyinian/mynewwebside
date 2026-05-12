package com.mywebside.blog.stock;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockTradeRepository extends JpaRepository<StockTrade, Long> {
  List<StockTrade> findByUserIdOrderByTradedAtDesc(Long userId);
}
