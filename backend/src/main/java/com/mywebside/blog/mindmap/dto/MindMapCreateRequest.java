package com.mywebside.blog.mindmap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MindMapCreateRequest(
    @Size(max = 255) String title,
    @NotBlank String data
) {
}
