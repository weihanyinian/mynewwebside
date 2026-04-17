package com.mywebside.blog.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("oj_submission")
public class OjSubmissionEntity {

  @TableId(type = IdType.AUTO)
  private Long id;
  private Long userId;
  private String username;
  private String problemId;
  private String language;
  private String sourceCode;
  private Boolean submitted;
  private String verdict;
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
