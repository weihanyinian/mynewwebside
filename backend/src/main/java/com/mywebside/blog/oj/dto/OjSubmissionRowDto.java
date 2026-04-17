package com.mywebside.blog.oj.dto;

public record OjSubmissionRowDto(
    long id,
    String username,
    String problemId,
    String language,
    boolean submitted,
    String verdict,
    String message,
    Double timeSeconds,
    Integer memoryKb,
    String createdAt
) {
}
