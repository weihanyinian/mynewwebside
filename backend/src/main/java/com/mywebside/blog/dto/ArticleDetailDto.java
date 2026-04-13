package com.mywebside.blog.dto;

import java.time.Instant;
import java.util.List;

public record ArticleDetailDto(
    Long id,
    String title,
    String summary,
    String coverUrl,
    String status,
    String contentMd,
    long views,
    Instant publishedAt,
    CategoryDto category,
    List<TagDto> tags
) {}
