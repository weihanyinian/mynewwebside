package com.weihanyinian.website.service;

import com.weihanyinian.website.entity.Article;
import com.weihanyinian.website.repository.ArticleRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Transactional(readOnly = true)
    public Page<Article> getPublishedArticles(Pageable pageable) {
        return articleRepository.findByStatusOrderByCreatedAtDesc(Article.ArticleStatus.PUBLISHED, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Article> getPublishedArticlesByCategory(Long categoryId, Pageable pageable) {
        return articleRepository.findByCategoryIdAndStatusOrderByCreatedAtDesc(
                categoryId, Article.ArticleStatus.PUBLISHED, pageable);
    }

    @Transactional(readOnly = true)
    public Article getArticleById(Long id) {
        return articleRepository.findById(id).orElse(null);
    }

    @Transactional
    public void incrementViewCount(Long id) {
        articleRepository.findById(id).ifPresent(article -> {
            article.setViewCount(article.getViewCount() + 1);
            articleRepository.save(article);
        });
    }
}
