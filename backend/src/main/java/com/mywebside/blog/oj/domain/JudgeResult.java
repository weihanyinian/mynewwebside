package com.mywebside.blog.oj.domain;

/**
 * 统一判题/运行结果，供控制器序列化为 JSON。
 *
 * @param submitted   true 表示提交判题；false 表示仅运行样例/自定义输入
 * @param verdict     AC/WA/CE/RE/TLE；运行成功但未比对时为 RUN_OK
 * @param message     人类可读说明（如第几个用例失败）
 * @param timeSeconds Judge0 返回的 CPU 时间（秒），多测例取最大值
 * @param memoryKb    内存峰值 KB
 * @param stdout      最后一次运行的标准输出（提交失败时便于调试）
 * @param stderr      标准错误
 * @param compileOutput 编译输出
 */
public record JudgeResult(
    boolean submitted,
    String verdict,
    String message,
    Double timeSeconds,
    Integer memoryKb,
    String stdout,
    String stderr,
    String compileOutput
) {}
