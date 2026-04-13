package com.mywebside.blog.dto;

import java.time.Instant;
import java.util.List;

public record ArticleListItemDto(
    Long id,
    String title,
    String summary,
    String coverUrl,
    long views,
    Instant publishedAt,
    CategoryDto category,
    List<TagDto> tags
) {}
