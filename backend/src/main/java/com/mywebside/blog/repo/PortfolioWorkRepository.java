package com.mywebside.blog.repo;

import com.mywebside.blog.domain.PortfolioWork;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioWorkRepository extends JpaRepository<PortfolioWork, Long> {
  List<PortfolioWork> findAllByEnabledTrueOrderBySortOrderAscIdAsc();
  List<PortfolioWork> findAllByOrderBySortOrderAscIdAsc();
}
