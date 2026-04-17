package com.mywebside.blog.controller;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.common.PageResponse;
import com.mywebside.blog.oj.OjSubmissionService;
import com.mywebside.blog.oj.dto.OjSubmissionRowDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/oj/submissions")
public class AdminOjSubmissionController {

  private final OjSubmissionService submissionService;

  public AdminOjSubmissionController(OjSubmissionService submissionService) {
    this.submissionService = submissionService;
  }

  @GetMapping
  public ApiResponse<PageResponse<OjSubmissionRowDto>> page(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return ApiResponse.ok(submissionService.pageAll(page, size));
  }
}
