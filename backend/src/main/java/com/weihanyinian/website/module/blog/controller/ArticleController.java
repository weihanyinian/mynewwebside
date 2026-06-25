package com.weihanyinian.website.module.blog.controller;

import com.weihanyinian.website.common.ApiResponse;
import com.weihanyinian.website.module.blog.entity.Article;
import com.weihanyinian.website.module.blog.entity.Category;
import com.weihanyinian.website.module.blog.entity.Tag;
import com.weihanyinian.website.module.blog.service.ArticleService;
import com.weihanyinian.website.module.blog.service.MetaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/articles")
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

    @GetMapping("/articles/{id}")
    public ApiResponse<Article> getById(@PathVariable Long id) {
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return ApiResponse.error(404, "文章不存在");
        }
        articleService.incrementViewCount(id);
        return ApiResponse.success(article);
    }
}
