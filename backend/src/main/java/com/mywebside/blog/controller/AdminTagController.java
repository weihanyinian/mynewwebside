package com.mywebside.blog.controller;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.dto.TagDto;
import com.mywebside.blog.dto.TagUpsertRequest;
import com.mywebside.blog.service.TagService;
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
@RequestMapping("/api/admin/tags")
public class AdminTagController {
  private final TagService tagService;

  public AdminTagController(TagService tagService) {
    this.tagService = tagService;
  }

  @GetMapping
  public ApiResponse<List<TagDto>> list() {
    return ApiResponse.ok(tagService.list());
  }

  @PostMapping
  public ApiResponse<TagDto> create(@Valid @RequestBody TagUpsertRequest req) {
    return ApiResponse.ok(tagService.create(req.name()));
  }

  @PutMapping("/{id}")
  public ApiResponse<TagDto> update(@PathVariable long id, @Valid @RequestBody TagUpsertRequest req) {
    return ApiResponse.ok(tagService.update(id, req.name()));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable long id) {
    tagService.delete(id);
    return ApiResponse.ok();
  }
}
