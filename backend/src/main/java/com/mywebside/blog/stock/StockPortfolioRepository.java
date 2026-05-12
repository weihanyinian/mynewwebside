package com.mywebside.blog.stock;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPortfolioRepository extends JpaRepository<StockPortfolio, Long> {
  List<StockPortfolio> findByUserId(Long userId);
  Optional<StockPortfolio> findByUserIdAndStockCode(Long userId, String stockCode);
  long countByUserId(Long userId);
}
