package com.mywebside.blog.oj.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

/**
 * 与 {@code oj/problems.json} 结构一致；评测用例仅服务端使用，不对外序列化。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Problem(
    String id,
    String title,
    String difficulty,
    String judgeMode,
    String description,
    String inputDesc,
    String outputDesc,
    int timeLimitSec,
    int memoryLimitMb,
    List<String> supportedLangs,
    List<TestCase> samples,
    List<TestCase> testCases,
    Map<String, String> referenceSolution
) {
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record TestCase(String input, String output) {}
}
