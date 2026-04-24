package com.mywebside.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
    @NotBlank @Size(max = 50) String author,
    @NotBlank @Size(max = 1000) String content
) {}
