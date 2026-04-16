package com.mywebside.blog.dto;

import com.mywebside.blog.domain.WallMessageStatus;
import java.time.Instant;

/** 后台管理列表项。 */
public record WallMessageAdminDto(
    Long id,
    String nickname,
    String content,
    WallMessageStatus status,
    String adminReply,
    Instant createdAt,
    Instant reviewedAt
) {}
