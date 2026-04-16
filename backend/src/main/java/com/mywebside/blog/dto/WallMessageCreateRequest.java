package com.mywebside.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 访客提交留言。 */
public record WallMessageCreateRequest(
    @Size(max = 64) String nickname,
    @NotBlank @Size(max = 2000) String content
) {}
