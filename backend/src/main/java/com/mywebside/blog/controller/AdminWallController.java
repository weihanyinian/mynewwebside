package com.mywebside.blog.controller;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.common.PageResponse;
import com.mywebside.blog.domain.WallMessageStatus;
import com.mywebside.blog.dto.WallMessageAdminDto;
import com.mywebside.blog.dto.WallMessageReplyRequest;
import com.mywebside.blog.service.WallMessageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 留言墙管理：审核、回复、删除。
 * <p>需 JWT 且角色 ADMIN（见 SecurityConfig）。</p>
 */
@RestController
@RequestMapping("/api/admin/wall/messages")
public class AdminWallController {

  private final WallMessageService wallMessageService;

  public AdminWallController(WallMessageService wallMessageService) {
    this.wallMessageService = wallMessageService;
  }

  @GetMapping
  public ApiResponse<PageResponse<WallMessageAdminDto>> page(
      @RequestParam(required = false) WallMessageStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return ApiResponse.ok(wallMessageService.pageAdmin(status, page, size));
  }

  @PatchMapping("/{id}/approve")
  public ApiResponse<Void> approve(@PathVariable Long id) {
    wallMessageService.approve(id);
    return ApiResponse.ok();
  }

  @PatchMapping("/{id}/reject")
  public ApiResponse<Void> reject(@PathVariable Long id) {
    wallMessageService.reject(id);
    return ApiResponse.ok();
  }

  @PatchMapping("/{id}/reply")
  public ApiResponse<Void> reply(@PathVariable Long id, @Valid @RequestBody WallMessageReplyRequest req) {
    wallMessageService.reply(id, req.reply());
    return ApiResponse.ok();
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Long id) {
    wallMessageService.delete(id);
    return ApiResponse.ok();
  }
}
