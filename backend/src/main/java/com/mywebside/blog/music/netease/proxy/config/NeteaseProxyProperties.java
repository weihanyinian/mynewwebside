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
}
