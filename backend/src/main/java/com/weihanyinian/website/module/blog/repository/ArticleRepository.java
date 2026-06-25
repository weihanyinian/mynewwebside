package com.weihanyinian.website.module.blog.repository;

import com.weihanyinian.website.module.blog.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    Page<Article> findByStatusOrderByCreatedAtDesc(Article.ArticleStatus status, Pageable pageable);
    Page<Article> findByCategoryIdAndStatusOrderByCreatedAtDesc(Long categoryId, Article.ArticleStatus status, Pageable pageable);
}
