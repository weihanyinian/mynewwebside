package com.mywebsite.blog.dto;

import java.time.Instant;

public record PortfolioWorkAdminDto(
    long id,
    String title,
    String desc,
    String detail,
    String contentMd,
    String tag,
    String link,
    String demoUrl,
    String repoUrl,
    String techStack,
    String cover,
    boolean enabled,
    int sortOrder,
    Instant createdAt,
    Instant updatedAt
) {}
