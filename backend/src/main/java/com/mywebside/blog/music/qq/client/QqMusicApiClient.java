package com.mywebside.blog.music.qq.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mywebside.blog.music.qq.config.QqMusicProxyProperties;
import java.time.Duration;
import java.net.http.HttpClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 调用本地/容器内 {@link QqMusicProxyProperties#getBaseUrl()} 部署的
 * <a href="https://github.com/jsososo/QQMusicApi">QQMusicApi</a>。
 */
@Component
public class QqMusicApiClient {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final RestClient restClient;

  public QqMusicApiClient(QqMusicProxyProperties properties) {
    String base = normalizeBaseUrl(properties.getBaseUrl());
    if (base.isBlank()) {
      throw new IllegalStateException("qq.proxy.base-url 未配置");
    }
    HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(3))
        .build();
    JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
    factory.setReadTimeout(Duration.ofSeconds(12));
    this.restClient = RestClient.builder()
        .baseUrl(base)
        .requestFactory(factory)
        .build();
  }

  /**
   * QQ 音乐搜索。{@code t} 与上游一致：0 单曲、2 歌单、8 专辑、9 歌手（见 QQMusicApi {@code routes/search.js}）。
   */
  public JsonNode search(String keyword, int pageNo, int pageSize, int t) throws RestClientException {
    String uri = UriComponentsBuilder.fromPath("/search/")
        .queryParam("key", keyword)
        .queryParam("pageNo", pageNo)
        .queryParam("pageSize", Math.min(Math.max(pageSize, 1), 50))
        .queryParam("t", t)
        .build(true)
        .toUriString();
    return getJson(uri, null);
  }

  public JsonNode songUrl(String songmid, String type, String cookieHeaderOrNull) throws RestClientException {
    String uri = UriComponentsBuilder.fromPath("/song/url")
        .queryParam("id", songmid)
        .queryParam("type", type != null && !type.isBlank() ? type : "128")
        .queryParam("ownCookie", 1)
        .build(true)
        .toUriString();
    return getJson(uri, cookieHeaderOrNull);
  }

  /** QQ 音乐巅峰榜，对应上游 {@code GET /top/}。 */
  public JsonNode topList(int topId, int pageNo, int pageSize) throws RestClientException {
    String uri = UriComponentsBuilder.fromPath("/top/")
        .queryParam("id", topId)
        .queryParam("pageNo", Math.max(1, pageNo))
        .queryParam("pageSize", Math.min(Math.max(pageSize, 1), 100))
        .build(true)
        .toUriString();
    return getJson(uri, null);
  }

  public JsonNode lyric(String songmid) throws RestClientException {
    String uri = UriComponentsBuilder.fromPath("/lyric/")
        .queryParam("songmid", songmid)
        .build(true)
        .toUriString();
    return getJson(uri, null);
  }

  /** 校验 Cookie 并尝试拉取昵称（公开主页接口）。 */
  public JsonNode userDetail(String qqUin, String cookieHeaderOrNull) throws RestClientException {
    String uri = UriComponentsBuilder.fromPath("/user/detail")
        .queryParam("id", qqUin)
        .build(true)
        .toUriString();
    return getJson(uri, cookieHeaderOrNull);
  }

  private JsonNode getJson(String relativeUri, String cookieHeaderOrNull) {
    String body = restClient.get()
        .uri(relativeUri)
        .headers(h -> {
          h.set(HttpHeaders.ACCEPT_ENCODING, "identity");
          if (cookieHeaderOrNull != null && !cookieHeaderOrNull.isBlank()) {
            h.add(HttpHeaders.COOKIE, cookieHeaderOrNull);
          }
        })
        .retrieve()
        .body(String.class);
    if (body == null || body.isBlank()) {
      return MAPPER.createObjectNode();
    }
    try {
      return MAPPER.readTree(body);
    } catch (Exception e) {
      throw new RestClientException("parse qq api json failed", e);
    }
  }

  private static String normalizeBaseUrl(String raw) {
    if (raw == null) {
      return "";
    }
    String base = raw.trim();
    if (base.endsWith("/")) {
      return base.substring(0, base.length() - 1);
    }
    return base;
  }
}
