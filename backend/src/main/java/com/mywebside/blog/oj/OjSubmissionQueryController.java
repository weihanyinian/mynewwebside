package com.mywebsite.blog.oj;

import com.mywebsite.blog.common.ApiResponse;
import com.mywebsite.blog.common.PageResponse;
import com.mywebsite.blog.oj.dto.OjSubmissionRowDto;
import com.mywebsite.blog.service.UserAccountService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oj")
public class OjSubmissionQueryController {

  private final OjSubmissionService submissionService;
  private final UserAccountService userAccountService;

  public OjSubmissionQueryController(OjSubmissionService submissionService, UserAccountService userAccountService) {
    this.submissionService = submissionService;
    this.userAccountService = userAccountService;
  }

  @GetMapping("/submissions/mine")
  public ApiResponse<PageResponse<OjSubmissionRowDto>> mine(
      Authentication auth,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    long uid = userAccountService.requireUserId(auth.getName());
    return ApiResponse.ok(submissionService.pageMine(uid, page, size));
  }
}
