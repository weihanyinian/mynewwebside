package com.mywebside.blog.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockOrderRepository extends JpaRepository<StockOrder, Long> {
  List<StockOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
  List<StockOrder> findByStatusInOrderByCreatedAtAsc(List<StockOrder.OrderStatus> statuses);
}
