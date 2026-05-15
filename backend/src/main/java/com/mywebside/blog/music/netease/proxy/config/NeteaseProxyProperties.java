package com.mywebside.blog.music.netease.proxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 网易云第三方 Binaryify 类 API 根地址（可自建或部署），用于登录、歌单、播放链接等。
 * <p>推荐自建 <a href="https://github.com/NeteaseCloudMusicApiEnhanced/api-enhanced">api-enhanced</a>
 *（npm {@code @neteasecloudmusicapienhanced/api}，本仓库 {@code ncm-api/} 或 Docker {@code moefurina/ncm-api}）。</p>
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
   * 公开「热歌」榜歌单 ID（与网页分享 {@code ?id=} 一致），默认云音乐热歌榜。
   */
  private String hotChartPlaylistId = "3778678";

  /**
   * 默认音质码率（单位 bps），常见值：128000/192000/320000/999000（需账号与版权支持）。
   */
  private int defaultBr = 320000;

  /**
   * 上游失败时的重试次数（不含首次）。
   */
  private int retryCount = 1;

  /**
   * 是否启用备用 {@link #fallbackBaseUrl}：主代理连接失败、HTTP 错误等时会自动尝试一次备用地址。
   */
  private boolean localFallbackEnabled = true;

  /**
   * 主代理不可达或业务失败时尝试的备用根地址（须与 {@link #baseUrl} 不同才会启用）。
   * 默认本机 api-enhanced（3000）；公网镜像易失效时请先本地 {@code npm start}。
   */
  private String fallbackBaseUrl = "http://127.0.0.1:3000";

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

  public String getHotChartPlaylistId() {
    return hotChartPlaylistId;
  }

  public void setHotChartPlaylistId(String hotChartPlaylistId) {
    this.hotChartPlaylistId =
        hotChartPlaylistId != null && !hotChartPlaylistId.isBlank() ? hotChartPlaylistId.trim() : "3778678";
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
