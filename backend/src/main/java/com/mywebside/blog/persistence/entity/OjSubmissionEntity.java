package com.mywebside.blog.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "oj_submission")
public class OjSubmissionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId;

  @Column(length = 64)
  private String username;

  @Column(length = 32)
  private String problemId;

  @Column(length = 16)
  private String language;

  @Column(columnDefinition = "MEDIUMTEXT")
  private String sourceCode;

  private Boolean submitted;

  @Column(length = 32)
  private String verdict;

  @Column(length = 4000)
  private String message;

  private Double timeSeconds;

  private Integer memoryKb;

  private LocalDateTime createdAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getProblemId() {
    return problemId;
  }

  public void setProblemId(String problemId) {
    this.problemId = problemId;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getSourceCode() {
    return sourceCode;
  }

  public void setSourceCode(String sourceCode) {
    this.sourceCode = sourceCode;
  }

  public Boolean getSubmitted() {
    return submitted;
  }

  public void setSubmitted(Boolean submitted) {
    this.submitted = submitted;
  }

  public String getVerdict() {
    return verdict;
  }

  public void setVerdict(String verdict) {
    this.verdict = verdict;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Double getTimeSeconds() {
    return timeSeconds;
  }

  public void setTimeSeconds(Double timeSeconds) {
    this.timeSeconds = timeSeconds;
  }

  public Integer getMemoryKb() {
    return memoryKb;
  }

  public void setMemoryKb(Integer memoryKb) {
    this.memoryKb = memoryKb;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
