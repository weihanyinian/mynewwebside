package com.mywebside.blog.dto;

import java.time.Instant;

public record CodeSnippetDto(
    long id,
    String title,
    String language,
    String content,
    Instant updatedAt
) {}
