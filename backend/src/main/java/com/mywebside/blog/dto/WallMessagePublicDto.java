package com.mywebside.blog.dto;

import java.time.Instant;

/** 前台可见留言（仅已通过审核）。 */
public record WallMessagePublicDto(Long id, String nickname, String content, String adminReply, Instant createdAt) {}
