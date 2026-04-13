package com.mywebside.blog.controller;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.common.PageResponse;
import com.mywebside.blog.dto.AdminArticleListItemDto;
import com.mywebside.blog.dto.ArticleDetailDto;
import com.mywebside.blog.dto.ArticleUpsertRequest;
import com.mywebside.blog.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/articles")
public class AdminArticleController {
  private final ArticleService articleService;

  public AdminArticleController(ArticleService articleService) {
    this.articleService = articleService;
  }

  @GetMapping
  public ApiResponse<PageResponse<AdminArticleListItemDto>> page(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    return ApiResponse.ok(articleService.pageAdmin(keyword, status, page, size));
  }

  @GetMapping("/{id}")
  public ApiResponse<ArticleDetailDto> detail(@PathVariable long id) {
    return ApiResponse.ok(articleService.getAdminDetail(id));
  }

  @PostMapping
  public ApiResponse<ArticleDetailDto> create(@Valid @RequestBody ArticleUpsertRequest req) {
    return ApiResponse.ok(articleService.create(req));
  }

  @PutMapping("/{id}")
  public ApiResponse<ArticleDetailDto> update(@PathVariable long id, @Valid @RequestBody ArticleUpsertRequest req) {
    return ApiResponse.ok(articleService.update(id, req));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable long id) {
    articleService.delete(id);
    return ApiResponse.ok();
  }
}
