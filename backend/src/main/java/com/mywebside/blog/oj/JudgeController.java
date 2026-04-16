package com.mywebside.blog.oj;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.oj.domain.JudgeResult;
import com.mywebside.blog.oj.dto.JudgeRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 对接 Judge0：运行 / 提交判题。 */
@RestController
@RequestMapping("/api/oj")
public class JudgeController {

  private final OjJudgeService judgeService;

  public JudgeController(OjJudgeService judgeService) {
    this.judgeService = judgeService;
  }

  @PostMapping("/judge")
  public ApiResponse<JudgeResult> judge(@Valid @RequestBody JudgeRequest req) {
    return ApiResponse.ok(judgeService.judge(req));
  }
}
