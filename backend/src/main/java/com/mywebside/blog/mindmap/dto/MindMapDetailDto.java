package com.mywebside.blog.mindmap.dto;

import java.time.LocalDateTime;

public record MindMapDetailDto(long id, String title, String data, LocalDateTime updatedAt) {
}
