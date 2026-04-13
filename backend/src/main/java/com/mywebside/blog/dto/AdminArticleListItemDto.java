package com.mywebside.blog.dto;

import java.time.Instant;
import java.util.List;

public record AdminArticleListItemDto(
    Long id,
    String title,
    String summary,
    String coverUrl,
    String status,
    long views,
    Instant createdAt,
    Instant updatedAt,
    Instant publishedAt,
    CategoryDto category,
    List<TagDto> tags
) {}
