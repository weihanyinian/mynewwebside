package com.mywebside.blog.music.netease.proxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 网易云第三方 Binaryify 类 API 根地址（可自建或部署），用于登录、歌单、播放链接等。
 */
@ConfigurationProperties(prefix = "netease.proxy")
public class NeteaseProxyProperties {

  /**
   * 与 {@link com.mywebside.blog.controller.MusicController} 历史默认一致，可通过环境变量覆盖。
   */
  private String baseUrl = "https://netease-cloud-music-api-five-roan-58.vercel.app";

  /**
   * 公开歌单默认 ID（对应网页分享链接中的 {@code ?id=}），例如「我喜欢的音乐」外链歌单。
   */
  private String defaultPlaylistId = "489057279";

  /**
   * 默认音质码率（单位 bps），常见值：128000/192000/320000/999000（需账号与版权支持）。
   */
  private int defaultBr = 320000;

  /**
   * 上游失败时的重试次数（不含首次）。
   */
  private int retryCount = 1;

  /**
   * 当主代理（例如 localhost:3000）不可达时，是否自动回退到公共代理。
   */
  private boolean localFallbackEnabled = true;

  /**
   * 本地代理不可达时使用的回退地址。
   */
  private String fallbackBaseUrl = "https://netease-cloud-music-api-five-roan-58.vercel.app";

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl != null ? baseUrl.trim() : "";
  }

  public String getDefaultPlaylistId() {
    return defaultPlaylistId;
  }

  public void setDefaultPlaylistId(String defaultPlaylistId) {
    this.defaultPlaylistId = defaultPlaylistId != null ? defaultPlaylistId.trim() : "489057279";
  }

  public int getDefaultBr() {
    return defaultBr;
  }

  public void setDefaultBr(int defaultBr) {
    this.defaultBr = Math.max(64000, defaultBr);
  }

  public int getRetryCount() {
    return retryCount;
  }

  public void setRetryCount(int retryCount) {
    this.retryCount = Math.max(0, retryCount);
  }

  public boolean isLocalFallbackEnabled() {
    return localFallbackEnabled;
  }

  public void setLocalFallbackEnabled(boolean localFallbackEnabled) {
    this.localFallbackEnabled = localFallbackEnabled;
  }

  public String getFallbackBaseUrl() {
    return fallbackBaseUrl;
  }

  public void setFallbackBaseUrl(String fallbackBaseUrl) {
    this.fallbackBaseUrl = fallbackBaseUrl != null ? fallbackBaseUrl.trim() : "";
  }
}
