package com.mywebside.blog.oj;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.oj.domain.JudgeResult;
import com.mywebside.blog.oj.dto.JudgeRequest;
import com.mywebside.blog.service.UserAccountService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 对接 Judge0：运行 / 提交判题（需登录）；异步提交 + 轮询结果。 */
@RestController
@RequestMapping("/api/oj")
public class JudgeController {

  private final OjJudgeService judgeService;
  private final OjSubmissionService submissionService;
  private final UserAccountService userAccountService;
  private final JudgeTaskStore taskStore;

  public JudgeController(
      OjJudgeService judgeService,
      OjSubmissionService submissionService,
      UserAccountService userAccountService,
      JudgeTaskStore taskStore
  ) {
    this.judgeService = judgeService;
    this.submissionService = submissionService;
    this.userAccountService = userAccountService;
    this.taskStore = taskStore;
  }

  /** 异步提交判题：立即返回 taskId（202 Accepted），后台执行判题。 */
  @PostMapping("/judge")
  public ResponseEntity<ApiResponse<Map<String, String>>> judge(
      Authentication auth,
      @Valid @RequestBody JudgeRequest req
  ) {
    String username = auth.getName();
    long userId = userAccountService.requireUserId(username);

    String taskId = UUID.randomUUID().toString().replace("-", "");
    taskStore.putPending(taskId);

    // 异步执行判题
    Thread.ofVirtual().start(() -> {
      try {
        JudgeResult r = judgeService.judge(req);
        taskStore.putResult(taskId, r);
        submissionService.record(userId, username, req, r);
      } catch (Exception e) {
        JudgeResult err = new JudgeResult(
            Boolean.TRUE.equals(req.submit()),
            "SE",
            e instanceof BusinessException be ? be.getMessage() : "判题服务异常",
            null, null, "", "", ""
        );
        taskStore.putResult(taskId, err);
      }
    });

    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(ApiResponse.ok(Map.of("taskId", taskId)));
  }

  /** 轮询判题结果。pending 时返回 verdict=PENDING，前端可据此继续轮询。 */
  @GetMapping("/judge/{taskId}")
  public ApiResponse<JudgeResult> pollResult(@PathVariable String taskId) {
    JudgeTaskStore.Task task = taskStore.get(taskId);
    if (task == null) {
      throw new BusinessException(404, "任务不存在或已过期");
    }
    if (!task.isDone()) {
      return ApiResponse.ok(new JudgeResult(false, "PENDING", "判题中，请稍候…", null, null, "", "", ""));
    }
    return ApiResponse.ok(task.result());
  }
}
