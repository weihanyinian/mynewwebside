package com.mywebside.blog.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("oj_problem")
public class OjProblemEntity {

  @TableId
  private String id;
  private String title;
  private String difficulty;
  private String judgeMode;
  private String description;
  private String inputDesc;
  private String outputDesc;
  private Integer timeLimitSec;
  private Integer memoryLimitMb;
  /** JSON 数组，如 ["C","CPP"] */
  private String supportedLangsJson;
  private String samplesJson;
  private String testCasesJson;
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
