package com.mywebside.blog.dto;

import java.time.Instant;

public record PortfolioWorkAdminDto(
    long id,
    String title,
    String desc,
    String detail,
    String tag,
    String link,
    String cover,
    boolean enabled,
    int sortOrder,
    Instant createdAt,
    Instant updatedAt
) {}
