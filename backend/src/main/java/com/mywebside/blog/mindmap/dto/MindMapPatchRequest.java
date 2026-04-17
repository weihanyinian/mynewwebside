package com.mywebside.blog.mindmap.dto;

import jakarta.validation.constraints.Size;

public record MindMapPatchRequest(
    @Size(max = 255) String title,
    String data
) {
}
