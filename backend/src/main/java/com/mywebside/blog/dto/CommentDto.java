package com.mywebside.blog.dto;

import java.time.Instant;

public record CommentDto(
    Long id,
    String author,
    String content,
    Instant createTime
) {
}