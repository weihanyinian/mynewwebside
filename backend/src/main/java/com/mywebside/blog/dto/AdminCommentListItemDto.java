package com.mywebsite.blog.dto;

import java.time.Instant;

public record AdminCommentListItemDto(
    long id,
    long articleId,
    String articleTitle,
    String author,
    String content,
    Instant createTime
) {}
