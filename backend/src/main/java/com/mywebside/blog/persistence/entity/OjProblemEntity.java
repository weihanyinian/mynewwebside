package com.mywebside.blog.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "oj_problem")
public class OjProblemEntity {

  @Id
  @Column(length = 32)
  private String id;

  @Column(length = 128)
  private String title;

  @Column(length = 16)
  private String difficulty;

  @Column(length = 16)
  private String judgeMode;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(columnDefinition = "TEXT")
  private String inputDesc;

  @Column(columnDefinition = "TEXT")
  private String outputDesc;

  private Integer timeLimitSec;

  private Integer memoryLimitMb;

  /** JSON 数组，如 ["C","CPP"] */
  @Column(columnDefinition = "TEXT")
  private String supportedLangsJson;

  @Column(columnDefinition = "TEXT")
  private String samplesJson;

  @Column(columnDefinition = "TEXT")
  private String testCasesJson;

  @Column(columnDefinition = "TEXT")
  private String referenceSolutionJson;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDifficulty() {
    return difficulty;
  }

  public void setDifficulty(String difficulty) {
    this.difficulty = difficulty;
  }

  public String getJudgeMode() {
    return judgeMode;
  }

  public void setJudgeMode(String judgeMode) {
    this.judgeMode = judgeMode;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getInputDesc() {
    return inputDesc;
  }

  public void setInputDesc(String inputDesc) {
    this.inputDesc = inputDesc;
  }

  public String getOutputDesc() {
    return outputDesc;
  }

  public void setOutputDesc(String outputDesc) {
    this.outputDesc = outputDesc;
  }

  public Integer getTimeLimitSec() {
    return timeLimitSec;
  }

  public void setTimeLimitSec(Integer timeLimitSec) {
    this.timeLimitSec = timeLimitSec;
  }

  public Integer getMemoryLimitMb() {
    return memoryLimitMb;
  }

  public void setMemoryLimitMb(Integer memoryLimitMb) {
    this.memoryLimitMb = memoryLimitMb;
  }

  public String getSupportedLangsJson() {
    return supportedLangsJson;
  }

  public void setSupportedLangsJson(String supportedLangsJson) {
    this.supportedLangsJson = supportedLangsJson;
  }

  public String getSamplesJson() {
    return samplesJson;
  }

  public void setSamplesJson(String samplesJson) {
    this.samplesJson = samplesJson;
  }

  public String getTestCasesJson() {
    return testCasesJson;
  }

  public void setTestCasesJson(String testCasesJson) {
    this.testCasesJson = testCasesJson;
  }

  public String getReferenceSolutionJson() {
    return referenceSolutionJson;
  }

  public void setReferenceSolutionJson(String referenceSolutionJson) {
    this.referenceSolutionJson = referenceSolutionJson;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
