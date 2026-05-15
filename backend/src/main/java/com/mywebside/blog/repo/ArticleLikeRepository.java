package com.mywebside.blog.repo;

import com.mywebside.blog.domain.ArticleLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {
  long countByArticleId(Long articleId);
  boolean existsByArticleIdAndUserId(Long articleId, Long userId);
  Optional<ArticleLike> findByArticleIdAndUserId(Long articleId, Long userId);
}
