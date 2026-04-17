package com.mywebside.blog.oj;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.oj.domain.JudgeResult;
import com.mywebside.blog.oj.dto.JudgeRequest;
import com.mywebside.blog.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 对接 Judge0：运行 / 提交判题（需登录）；每次调用写入提交记录。 */
@RestController
@RequestMapping("/api/oj")
public class JudgeController {

  private final OjJudgeService judgeService;
  private final OjSubmissionService submissionService;
  private final UserAccountService userAccountService;

  public JudgeController(
      OjJudgeService judgeService,
      OjSubmissionService submissionService,
      UserAccountService userAccountService
  ) {
    this.judgeService = judgeService;
    this.submissionService = submissionService;
    this.userAccountService = userAccountService;
  }

  @PostMapping("/judge")
  public ApiResponse<JudgeResult> judge(Authentication auth, @Valid @RequestBody JudgeRequest req) {
    String username = auth.getName();
    long userId = userAccountService.requireUserId(username);
    JudgeResult r = judgeService.judge(req);
    submissionService.record(userId, username, req, r);
    return ApiResponse.ok(r);
  }
}
