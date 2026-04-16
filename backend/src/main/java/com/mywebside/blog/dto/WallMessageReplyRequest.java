package com.mywebside.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 站长回复（审核通过后也可补充）。 */
public record WallMessageReplyRequest(@NotBlank @Size(max = 1000) String reply) {}
