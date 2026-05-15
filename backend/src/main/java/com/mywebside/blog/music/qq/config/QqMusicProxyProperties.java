package com.mywebside.blog.music.qq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "qq.proxy")
public class QqMusicProxyProperties {

  /**
   * sansenjian/qq-music-api 服务根地址，如 http://127.0.0.1:3200
   *
   * @see <a href="https://sansenjian.github.io/qq-music-api/api/">API 文档</a>
   * @see <a href="https://github.com/sansenjian/qq-music-api">GitHub</a>
   */
  private String baseUrl = "http://127.0.0.1:3200";

  /**
   * 排行榜 {@code topId}，与 {@code GET /getRanks?topId=} 一致；默认 62（热歌榜）。
   * 常见：4 飙升、62 热歌、208 新歌、6 原创（见在线文档「排行榜 API」）。
   */
  private int hotChartTopId = 62;

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public int getHotChartTopId() {
    return hotChartTopId;
  }

  public void setHotChartTopId(int hotChartTopId) {
    this.hotChartTopId = hotChartTopId > 0 ? hotChartTopId : 62;
  }
}
