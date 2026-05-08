package com.mywebside.blog.controller;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.common.PageResponse;
import com.mywebside.blog.dto.ArticleDetailDto;
import com.mywebside.blog.dto.ArticleListItemDto;
import com.mywebside.blog.dto.CategoryDto;
import com.mywebside.blog.dto.DailyQuoteDto;
import com.mywebside.blog.dto.TagDto;
import com.mywebside.blog.service.ArticleService;
import com.mywebside.blog.service.CategoryService;
import com.mywebside.blog.service.DailyQuoteService;
import com.mywebside.blog.service.TagService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 访客只读资源（REST 前缀 {@code /api/public}）。
 * <p>统一响应体为 {@link com.mywebside.blog.common.ApiResponse}，错误由全局异常处理转换为 HTTP 状态码 + JSON。</p>
 */
@RestController
@RequestMapping("/api/public")
public class PublicController {
  private final ArticleService articleService;
  private final CategoryService categoryService;
  private final TagService tagService;
  private final DailyQuoteService dailyQuoteService;

  public PublicController(
      ArticleService articleService,
      CategoryService categoryService,
      TagService tagService,
      DailyQuoteService dailyQuoteService
  ) {
    this.articleService = articleService;
    this.categoryService = categoryService;
    this.tagService = tagService;
    this.dailyQuoteService = dailyQuoteService;
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

  /** 每日一句（与前端本地缓存配合，同日多次请求内容一致）。 */
  @GetMapping("/daily-quote")
  public ApiResponse<DailyQuoteDto> dailyQuote() {
    return ApiResponse.ok(dailyQuoteService.today());
  }
}
