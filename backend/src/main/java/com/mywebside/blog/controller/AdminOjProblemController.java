package com.mywebside.blog.controller;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.oj.OjProblemStore;
import com.mywebside.blog.oj.domain.Problem;
import com.mywebside.blog.oj.dto.ProblemListItemDto;
import com.mywebside.blog.persistence.mapper.OjProblemEntityMapper;
import java.util.List;
import java.util.Locale;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 管理员：OJ 题目 CRUD（含隐藏用例）。 */
@RestController
@RequestMapping("/api/admin/oj/problems")
public class AdminOjProblemController {

  private final OjProblemStore ojProblemStore;
  private final OjProblemEntityMapper problemMapper;

  public AdminOjProblemController(OjProblemStore ojProblemStore, OjProblemEntityMapper problemMapper) {
    this.ojProblemStore = ojProblemStore;
    this.problemMapper = problemMapper;
  }

  @GetMapping
  public ApiResponse<List<ProblemListItemDto>> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String difficulty
  ) {
    String qq = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
    String df = difficulty == null ? "" : difficulty.trim();
    List<ProblemListItemDto> list = ojProblemStore.all().stream()
        .filter(p -> qq.isEmpty() || p.title().toLowerCase(Locale.ROOT).contains(qq))
        .filter(p -> df.isEmpty() || p.difficulty().equals(df))
        .map(p -> new ProblemListItemDto(p.id(), p.title(), p.difficulty(), p.judgeMode()))
        .toList();
    return ApiResponse.ok(list);
  }

  /** 含 testCases，供后台编辑。 */
  @GetMapping("/{id}")
  public ApiResponse<Problem> detail(@PathVariable String id) {
    return ApiResponse.ok(ojProblemStore.require(id));
  }

  @PostMapping
  public ApiResponse<Void> create(@RequestBody Problem problem) {
    if (problemMapper.findById(problem.id()).isPresent()) {
      throw new BusinessException(400, "题目 ID 已存在");
    }
    ojProblemStore.upsertFromProblem(problem);
    return ApiResponse.ok();
  }

  @PutMapping("/{id}")
  public ApiResponse<Void> update(@PathVariable String id, @RequestBody Problem problem) {
    if (!id.equals(problem.id())) {
      throw new BusinessException(400, "路径与题目 id 不一致");
    }
    if (problemMapper.findById(id).isEmpty()) {
      throw new BusinessException(404, "题目不存在");
    }
    ojProblemStore.upsertFromProblem(problem);
    return ApiResponse.ok();
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable String id) {
    if (problemMapper.findById(id).isEmpty()) {
      throw new BusinessException(404, "题目不存在");
    }
    ojProblemStore.deleteById(id);
    return ApiResponse.ok();
  }
}
