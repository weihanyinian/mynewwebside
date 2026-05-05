package com.mywebsite.blog.music.qq.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "qq_user_session")
public class QqUserSessionEntity {

  @Id
  @Column(length = 64)
  private String username;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String cookieCipher;

  @Column(length = 32)
  private String qqUin;

  @Column(length = 128)
  private String qqNickname;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getCookieCipher() {
    return cookieCipher;
  }

  public void setCookieCipher(String cookieCipher) {
    this.cookieCipher = cookieCipher;
  }

  public String getQqUin() {
    return qqUin;
  }

  public void setQqUin(String qqUin) {
    this.qqUin = qqUin;
  }

  public String getQqNickname() {
    return qqNickname;
  }

  public void setQqNickname(String qqNickname) {
    this.qqNickname = qqNickname;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
