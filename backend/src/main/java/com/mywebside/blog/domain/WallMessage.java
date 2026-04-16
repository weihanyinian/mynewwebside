package com.mywebside.blog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

/** 留言墙实体（与文章评论 {@link Comment} 无关）。 */
@Entity
@Table(name = "wall_message")
public class WallMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 64)
  private String nickname;

  @Lob
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private WallMessageStatus status = WallMessageStatus.PENDING;

  @Column(length = 1000)
  private String adminReply;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  private Instant reviewedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public WallMessageStatus getStatus() {
    return status;
  }

  public void setStatus(WallMessageStatus status) {
    this.status = status;
  }

  public String getAdminReply() {
    return adminReply;
  }

  public void setAdminReply(String adminReply) {
    this.adminReply = adminReply;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getReviewedAt() {
    return reviewedAt;
  }

  public void setReviewedAt(Instant reviewedAt) {
    this.reviewedAt = reviewedAt;
  }
}
