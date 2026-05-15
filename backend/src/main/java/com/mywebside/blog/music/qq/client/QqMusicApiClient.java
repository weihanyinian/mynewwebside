package com.mywebside.blog.music.qq.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mywebside.blog.music.qq.config.QqMusicProxyProperties;
import java.nio.charset.StandardCharsets;
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
 * <a href="https://sansenjian.github.io/qq-music-api/api/">sansenjian/qq-music-api</a>
 *（默认端口 3200，见项目 README）。
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
   * 综合搜索，对应 {@code GET /getSearchByKey}。
   *
   * @see <a href="https://sansenjian.github.io/qq-music-api/api/search.html">搜索 API</a>
   */
  public JsonNode searchByKey(String keyword, int limit, int page, Integer catZhida) throws RestClientException {
    UriComponentsBuilder b = UriComponentsBuilder.fromPath("/getSearchByKey")
        .queryParam("key", keyword)
        .queryParam("limit", Math.min(Math.max(limit, 1), 50))
        .queryParam("page", Math.max(1, page));
    if (catZhida != null) {
      b.queryParam("catZhida", catZhida);
    }
    return getJson(b.build(false).encode(StandardCharsets.UTF_8).toUriString(), null);
  }

  /**
   * 播放地址，对应 {@code GET /getMusicPlay}。
   *
   * @see <a href="https://sansenjian.github.io/qq-music-api/api/music.html">音乐 API</a>
   */
  public JsonNode musicPlay(String songmid, Long songid, String cookieHeaderOrNull) throws RestClientException {
    UriComponentsBuilder b = UriComponentsBuilder.fromPath("/getMusicPlay")
        .queryParam("songmid", songmid);
    if (songid != null && songid > 0) {
      b.queryParam("songid", songid);
    }
    return getJson(b.build(false).encode(StandardCharsets.UTF_8).toUriString(), cookieHeaderOrNull);
  }

  /**
   * 排行榜，对应 {@code GET /getRanks}。
   *
   * @see <a href="https://sansenjian.github.io/qq-music-api/api/rank.html">排行榜 API</a>
   */
  public JsonNode getRanks(Integer topId, int limit, int page) throws RestClientException {
    UriComponentsBuilder b = UriComponentsBuilder.fromPath("/getRanks")
        .queryParam("limit", Math.min(Math.max(limit, 1), 100))
        .queryParam("page", Math.max(1, page));
    if (topId != null && topId > 0) {
      b.queryParam("topId", topId);
    }
    return getJson(b.build(false).encode(StandardCharsets.UTF_8).toUriString(), null);
  }

  /**
   * 歌词，对应 {@code GET /getLyric}；{@code isFormat=1} 返回解析后的 LRC 文本。
   */
  public JsonNode lyric(String songmid, boolean format) throws RestClientException {
    String uri = UriComponentsBuilder.fromPath("/getLyric")
        .queryParam("songmid", songmid)
        .queryParam("isFormat", format ? 1 : 0)
        .build(false)
        .encode(StandardCharsets.UTF_8)
        .toUriString();
    return getJson(uri, null);
  }

  /**
   * 智能搜索（smartbox），部分环境下比 {@link #searchByKey} 更稳定；对应 {@code GET /getSmartbox}。
   */
  public JsonNode getSmartbox(String keyword) throws RestClientException {
    String uri = UriComponentsBuilder.fromPath("/getSmartbox")
        .queryParam("key", keyword)
        .build(false)
        .encode(StandardCharsets.UTF_8)
        .toUriString();
    return getJson(uri, null);
  }

  /**
   * 用户歌单（校验 Cookie / 拉取基础资料），对应 {@code GET /user/getUserPlaylists}。
   *
   * @see <a href="https://sansenjian.github.io/qq-music-api/api/user.html">用户 API</a>
   */
  public JsonNode userPlaylists(String uin, int offset, int limit, String cookieHeaderOrNull) throws RestClientException {
    String uri = UriComponentsBuilder.fromPath("/user/getUserPlaylists")
        .queryParam("uin", uin)
        .queryParam("offset", Math.max(0, offset))
        .queryParam("limit", Math.min(Math.max(limit, 1), 50))
        .build(false)
        .encode(StandardCharsets.UTF_8)
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
