package com.mywebside.blog.dto;

import java.time.Instant;
import java.util.List;

public record CommentDto(
    Long id,
    String author,
    String content,
    Instant createTime,
    Long parentId,
    List<CommentDto> replies
) {
  public static CommentDto flat(Long id, String author, String content, Instant createTime, Long parentId) {
    return new CommentDto(id, author, content, createTime, parentId, List.of());
  }
}