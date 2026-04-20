package com.mywebside.blog.dto;

import java.time.Instant;

/** 访客提交后的回执（已入库并可立即展示）。 */
public record WallMessageSubmitVo(
    long id,
    String reviewHint,
    String nickname,
    String content,
    String adminReply,
    Instant createdAt
) {}
