package com.mywebside.blog.oj.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 判题/运行请求体。
 *
 * @param problemId  题目 ID
 * @param language   C / CPP / JAVA / PYTHON
 * @param sourceCode 完整源码（Java 须 {@code public class Main}）
 * @param stdin      仅运行模式有效；为空则使用题目第一个样例输入
 * @param submit     true：对所有隐藏用例判题；false：单次运行
 */
public record JudgeRequest(
    @NotBlank String problemId,
    @NotBlank String language,
    @NotBlank String sourceCode,
    String stdin,
    @NotNull Boolean submit
) {}
