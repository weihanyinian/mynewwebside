package com.mywebside.blog.controller;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.common.PageResponse;
import com.mywebside.blog.dto.ArticleDetailDto;
import com.mywebside.blog.dto.ArticleListItemDto;
import com.mywebside.blog.dto.CategoryDto;
import com.mywebside.blog.dto.TagDto;
import com.mywebside.blog.service.ArticleService;
import com.mywebside.blog.service.CategoryService;
import com.mywebside.blog.service.TagService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicController {
  private final ArticleService articleService;
  private final CategoryService categoryService;
  private final TagService tagService;

  public PublicController(ArticleService articleService, CategoryService categoryService, TagService tagService) {
    this.articleService = articleService;
    this.categoryService = categoryService;
    this.tagService = tagService;
  }

  @GetMapping("/articles")
  public ApiResponse<PageResponse<ArticleListItemDto>> listArticles(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) Long tagId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    return ApiResponse.ok(articleService.pagePublic(keyword, categoryId, tagId, page, size));
  }

  @GetMapping("/articles/{id}")
  public ApiResponse<ArticleDetailDto> getArticle(@PathVariable long id) {
    return ApiResponse.ok(articleService.getPublicDetail(id));
  }

  @GetMapping("/categories")
  public ApiResponse<List<CategoryDto>> categories() {
    return ApiResponse.ok(categoryService.list());
  }

  @GetMapping("/tags")
  public ApiResponse<List<TagDto>> tags() {
    return ApiResponse.ok(tagService.list());
  }
}
