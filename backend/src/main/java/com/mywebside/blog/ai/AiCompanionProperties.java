package com.mywebside.blog.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.companion")
public class AiCompanionProperties {
  private boolean enabled = false;
  private String baseUrl = "";
  private String model = "";
  private String apiKey = "";
  private int maxInputChars = 300;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public int getMaxInputChars() {
    return maxInputChars;
  }

  public void setMaxInputChars(int maxInputChars) {
    this.maxInputChars = maxInputChars;
  }
}
