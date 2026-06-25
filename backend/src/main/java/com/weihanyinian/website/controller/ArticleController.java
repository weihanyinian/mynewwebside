package com.weihanyinian.website.controller;

import com.weihanyinian.website.common.ApiResponse;
import com.weihanyinian.website.entity.Article;
import com.weihanyinian.website.service.ArticleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public ApiResponse<Page<Article>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Article> articles;
        if (categoryId != null) {
            articles = articleService.getPublishedArticlesByCategory(categoryId, pageable);
        } else {
            articles = articleService.getPublishedArticles(pageable);
        }
        return ApiResponse.success(articles);
    }

    @GetMapping("/{id}")
    public ApiResponse<Article> getById(@PathVariable Long id) {
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return ApiResponse.error(404, "文章不存在");
        }
        // Increment view count
        articleService.incrementViewCount(id);
        return ApiResponse.success(article);
    }
}
