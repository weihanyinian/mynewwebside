package com.mywebside.blog.music.qq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "qq.proxy")
public class QqMusicProxyProperties {

  /**
   * QQMusicApi 服务根地址，如 http://127.0.0.1:3300 或 Docker 内 http://qq-music-api:3300
   *
   * @see <a href="https://github.com/jsososo/QQMusicApi">jsososo/QQMusicApi</a>
   */
  private String baseUrl = "http://127.0.0.1:3300";

  /**
   * QQ 巅峰榜 {@code topId}，与 QQMusicApi {@code /top/} 一致；默认 4（流行指数榜，见 routes/top.js）。
   */
  private int hotChartTopId = 4;

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
    this.hotChartTopId = hotChartTopId > 0 ? hotChartTopId : 4;
  }
}
