package com.mywebside.blog.controller;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.dto.CategoryDto;
import com.mywebside.blog.dto.CategoryUpsertRequest;
import com.mywebside.blog.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {
  private final CategoryService categoryService;

  public AdminCategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @GetMapping
  public ApiResponse<List<CategoryDto>> list() {
    return ApiResponse.ok(categoryService.list());
  }

  @PostMapping
  public ApiResponse<CategoryDto> create(@Valid @RequestBody CategoryUpsertRequest req) {
    return ApiResponse.ok(categoryService.create(req.name()));
  }

  @PutMapping("/{id}")
  public ApiResponse<CategoryDto> update(@PathVariable long id, @Valid @RequestBody CategoryUpsertRequest req) {
    return ApiResponse.ok(categoryService.update(id, req.name()));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable long id) {
    categoryService.delete(id);
    return ApiResponse.ok();
  }
}
