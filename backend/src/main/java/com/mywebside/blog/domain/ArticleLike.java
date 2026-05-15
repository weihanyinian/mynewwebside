package com.mywebside.blog.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "article_like", uniqueConstraints = @UniqueConstraint(columnNames = {"article_id", "user_id"}))
public class ArticleLike {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "article_id", nullable = false)
  private Long articleId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "created_at")
  private Instant createdAt = Instant.now();

  public ArticleLike() {}
  public ArticleLike(Long articleId, Long userId) {
    this.articleId = articleId; this.userId = userId;
  }
  public Long getId() { return id; }
  public Long getArticleId() { return articleId; }
  public Long getUserId() { return userId; }
}
