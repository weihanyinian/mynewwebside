package com.mywebside.blog.controller;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.common.PageResponse;
import com.mywebside.blog.dto.WallMessageCreateRequest;
import com.mywebside.blog.dto.WallMessagePublicDto;
import com.mywebside.blog.dto.WallMessageSubmitVo;
import com.mywebside.blog.service.WallMessageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 留言墙公开接口（REST：资源名 wall/messages）。
 * <p>GET 仅返回已审核通过记录；POST 提交后进入待审核队列。</p>
 */
@RestController
@RequestMapping("/api/public/wall/messages")
public class PublicWallController {

  private final WallMessageService wallMessageService;

  public PublicWallController(WallMessageService wallMessageService) {
    this.wallMessageService = wallMessageService;
  }

  @GetMapping
  public ApiResponse<PageResponse<WallMessagePublicDto>> listApproved(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return ApiResponse.ok(wallMessageService.listApproved(page, size));
  }

  @PostMapping
  public ApiResponse<WallMessageSubmitVo> submit(@Valid @RequestBody WallMessageCreateRequest req) {
    return ApiResponse.ok(wallMessageService.submit(req));
  }
}
