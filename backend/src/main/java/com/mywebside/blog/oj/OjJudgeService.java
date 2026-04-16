package com.mywebside.blog.oj;

import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.oj.domain.JudgeResult;
import com.mywebside.blog.oj.domain.Problem;
import com.mywebside.blog.oj.dto.JudgeRequest;
import org.springframework.stereotype.Service;

/**
 * 统一判题：编译错误 / TLE / RE 直接映射；运行结束后自行比对输出得到 WA / AC。
 */
@Service
public class OjJudgeService {

  private final ProblemJsonStore problemStore;
  private final Judge0Service judge0Service;

  public OjJudgeService(ProblemJsonStore problemStore, Judge0Service judge0Service) {
    this.problemStore = problemStore;
    this.judge0Service = judge0Service;
  }

  public JudgeResult judge(JudgeRequest req) {
    Problem p = problemStore.find(req.problemId()).orElseThrow(() -> new BusinessException(404, "题目不存在"));
    if (p.supportedLangs().stream().noneMatch(l -> l.equalsIgnoreCase(req.language()))) {
      throw new BusinessException(400, "该题不支持语言：" + req.language());
    }
    int langId = Judge0Service.languageId(req.language());
    if (Boolean.FALSE.equals(req.submit())) {
      return runOnce(p, langId, req);
    }
    return submitAll(p, langId, req);
  }

  /** 单次运行：stdin 为空则用首个样例输入；不比对隐藏用例。 */
  private JudgeResult runOnce(Problem p, int langId, JudgeRequest req) {
    String in = req.stdin();
    if (in == null || in.isBlank()) {
      if (p.samples().isEmpty()) {
        throw new BusinessException(400, "题目无样例输入，请填写 stdin");
      }
      in = p.samples().get(0).input();
    }
    Judge0Service.RawOutcome raw = judge0Service.runOnce(p, langId, req.sourceCode(), in);
    String v = mapSystemVerdict(raw);
    if (v != null) {
      return new JudgeResult(false, v, describe(raw, v), raw.timeSeconds(), raw.memoryKb(), raw.stdout(), raw.stderr(), raw.compileOutput());
    }
    return new JudgeResult(false, "RUN_OK", "运行完成", raw.timeSeconds(), raw.memoryKb(), raw.stdout(), raw.stderr(), raw.compileOutput());
  }

  /** 对所有 testCases 依次评测，遇到系统错误立即返回；否则比对输出。 */
  private JudgeResult submitAll(Problem p, int langId, JudgeRequest req) {
    double maxTime = 0d;
    int maxMem = 0;
    for (int i = 0; i < p.testCases().size(); i++) {
      var tc = p.testCases().get(i);
      Judge0Service.RawOutcome raw = judge0Service.runOnce(p, langId, req.sourceCode(), tc.input());
      String sys = mapSystemVerdict(raw);
      if (sys != null) {
        return new JudgeResult(
            true,
            sys,
            "第 " + (i + 1) + " 个用例：" + describe(raw, sys),
            raw.timeSeconds(),
            raw.memoryKb(),
            raw.stdout(),
            raw.stderr(),
            raw.compileOutput()
        );
      }
      if (!OjOutputNormalize.same(tc.output(), raw.stdout())) {
        return new JudgeResult(
            true,
            "WA",
            "第 " + (i + 1) + " 个用例答案错误",
            raw.timeSeconds(),
            raw.memoryKb(),
            raw.stdout(),
            raw.stderr(),
            raw.compileOutput()
        );
      }
      maxTime = Math.max(maxTime, raw.timeSeconds());
      maxMem = Math.max(maxMem, raw.memoryKb());
    }
    return new JudgeResult(true, "AC", "Accepted", maxTime, maxMem, "", "", "");
  }

  /**
   * 将 Judge0 status.id 映射为 CE/RE/TLE；若程序正常结束返回 null（由业务层比对输出）。
   * 参考 Judge0：3 Accepted 表示进程正常结束，不代表答案正确。
   */
  private static String mapSystemVerdict(Judge0Service.RawOutcome raw) {
    int id = raw.statusId();
    if (id == 6) {
      return "CE";
    }
    if (id == 5) {
      return "TLE";
    }
    if (id >= 7 && id <= 12) {
      return "RE";
    }
    if (id != 3) {
      return "RE";
    }
    return null;
  }

  private static String describe(Judge0Service.RawOutcome raw, String verdict) {
    if ("CE".equals(verdict)) {
      String c = raw.compileOutput() == null ? "" : raw.compileOutput().strip();
      return (c.isBlank() ? "编译失败" : c);
    }
    if ("RE".equals(verdict)) {
      String e = raw.stderr() == null ? "" : raw.stderr().strip();
      String m = raw.statusDescription() == null ? "" : raw.statusDescription();
      if (!e.isBlank()) {
        return e;
      }
      return m.isBlank() ? "运行错误" : m;
    }
    if ("TLE".equals(verdict)) {
      return "超出时间限制";
    }
    return raw.statusDescription();
  }
}
