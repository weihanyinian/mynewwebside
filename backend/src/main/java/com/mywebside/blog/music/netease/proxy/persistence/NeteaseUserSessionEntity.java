package com.mywebside.blog.music.netease.proxy.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "netease_user_session")
public class NeteaseUserSessionEntity {

  @Id
  @Column(length = 64)
  private String username;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String cookieCipher;

  private Long neteaseUid;

  @Column(length = 128)
  private String neteaseNickname;

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

  public Long getNeteaseUid() {
    return neteaseUid;
  }

  public void setNeteaseUid(Long neteaseUid) {
    this.neteaseUid = neteaseUid;
  }

  public String getNeteaseNickname() {
    return neteaseNickname;
  }

  public void setNeteaseNickname(String neteaseNickname) {
    this.neteaseNickname = neteaseNickname;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
