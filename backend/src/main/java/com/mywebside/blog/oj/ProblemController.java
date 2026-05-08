package com.mywebside.blog.oj;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.oj.domain.Problem;
import com.mywebside.blog.oj.dto.ProblemDetailDto;
import com.mywebside.blog.oj.dto.ProblemListItemDto;
import java.util.List;
import java.util.Locale;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 题目查询：数据来自数据库 oj_problem。 */
@RestController
@RequestMapping("/api/oj")
public class ProblemController {

  private final OjProblemStore store;

  public ProblemController(OjProblemStore store) {
    this.store = store;
  }

  @GetMapping("/problems")
  public ApiResponse<List<ProblemListItemDto>> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String difficulty
  ) {
    String qq = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
    String df = difficulty == null ? "" : difficulty.trim();
    List<ProblemListItemDto> list = store.all().stream()
        .filter(p -> qq.isEmpty() || p.title().toLowerCase(Locale.ROOT).contains(qq))
        .filter(p -> df.isEmpty() || p.difficulty().equals(df))
        .map(p -> new ProblemListItemDto(p.id(), p.title(), p.difficulty(), p.judgeMode()))
        .toList();
    return ApiResponse.ok(list);
  }

  @GetMapping("/problems/{id}")
  public ApiResponse<ProblemDetailDto> detail(@PathVariable String id) {
    Problem p = store.find(id).orElseThrow(() -> new BusinessException(404, "题目不存在"));
    ProblemDetailDto dto = new ProblemDetailDto(
        p.id(),
        p.title(),
        p.difficulty(),
        p.judgeMode(),
        p.description(),
        p.inputDesc(),
        p.outputDesc(),
        p.timeLimitSec(),
        p.memoryLimitMb(),
        p.supportedLangs(),
        p.samples(),
        p.referenceSolution()
    );
    return ApiResponse.ok(dto);
  }
}
