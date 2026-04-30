package com.mywebsite.blog.repo;

import com.mywebsite.blog.domain.PortfolioWork;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioWorkRepository extends JpaRepository<PortfolioWork, Long> {
  List<PortfolioWork> findAllByEnabledTrueOrderBySortOrderAscIdAsc();
  List<PortfolioWork> findAllByOrderBySortOrderAscIdAsc();
  Optional<PortfolioWork> findByIdAndEnabledTrue(Long id);
}
