package com.mywebside.blog.mindmap.dto;

import java.time.LocalDateTime;

public record MindMapListItemDto(long id, String title, LocalDateTime updatedAt) {
}
