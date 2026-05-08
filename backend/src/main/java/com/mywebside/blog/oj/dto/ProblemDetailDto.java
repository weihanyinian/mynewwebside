package com.mywebside.blog.oj.dto;

import com.mywebside.blog.oj.domain.Problem;
import java.util.List;
import java.util.Map;

/**
 * 题目详情：对外不包含隐藏 {@code testCases}。
 */
public record ProblemDetailDto(
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
    List<Problem.TestCase> samples,
    Map<String, String> referenceSolution
) {}
