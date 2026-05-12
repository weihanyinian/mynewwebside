package com.mywebside.blog.music.netease.proxy.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mywebside.blog.music.netease.proxy.config.NeteaseProxyProperties;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.net.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 调用可配置的网易云第三方 API（与歌单代理同源），支持带 Cookie 的请求。
 */
@Component
public class NeteaseBinaryifyClient {

  private static final Logger log = LoggerFactory.getLogger(NeteaseBinaryifyClient.class);
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final RestClient restClient;
  private final RestClient fallbackRestClient;
  private final String primaryBaseUrl;
  private final NeteaseProxyProperties properties;

  public NeteaseBinaryifyClient(NeteaseProxyProperties properties) {
    this.properties = properties;
    String base = normalizeBaseUrl(properties.getBaseUrl());
    if (base.isBlank()) {
      throw new IllegalStateException("netease.proxy.base-url 未配置");
    }
    this.primaryBaseUrl = base;
    this.restClient = RestClient.builder()
        .baseUrl(base)
        .requestFactory(buildRequestFactory())
        .build();
    String fallbackBase = normalizeBaseUrl(properties.getFallbackBaseUrl());
    if (!fallbackBase.isBlank() && !fallbackBase.equalsIgnoreCase(base)) {
      this.fallbackRestClient = RestClient.builder()
          .baseUrl(fallbackBase)
          .requestFactory(buildRequestFactory())
          .build();
    } else {
      this.fallbackRestClient = null;
    }
  }

  private static JdkClientHttpRequestFactory buildRequestFactory() {
    HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(3))
        .build();
    JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
    // 增强版上游（解灰 / enhance player）偶发较慢；略放宽读超时，仍明显短于前端 axios 15s。
    factory.setReadTimeout(Duration.ofSeconds(12));
    return factory;
  }

  /**
   * 登录接口部分上游在业务失败时仍返回 HTTP 4xx/5xx，但响应体为 JSON（含 code/msg）。
   * {@link RestClient} 默认 {@code retrieve()} 遇非 2xx 会直接抛异常，导致无法解析正文。
   * 此处用 {@code exchange} 始终读取 body，与 Node 版 API 行为对齐。
   */
  /**
   * 调用增强版 {@code /login/cellphone}：与 npm {@code login_cellphone({ phone, password, countrycode })}
   * 一致，使用表单 POST（Express 合并 {@code req.body}）；明文密码由上游做 MD5。
   */
  public JsonNode loginCellphone(String phone, String password, String countrycode) throws RestClientException {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("phone", phone);
    form.add("password", password);
    form.add("countrycode", countrycode != null && !countrycode.isBlank() ? countrycode : "86");
    int maxAttempt = Math.max(1, properties.getRetryCount() + 1);
    RestClientException last = null;
    for (int attempt = 1; attempt <= maxAttempt; attempt++) {
      try {
        String body = restClient.post()
            .uri("/login/cellphone")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .headers(h -> h.set(HttpHeaders.ACCEPT_ENCODING, "identity"))
            .exchange((request, response) -> {
              try (response) {
                return StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
              }
            });
        if (body == null || body.isBlank()) {
          return MAPPER.createObjectNode();
        }
        return MAPPER.readTree(body);
      } catch (ResourceAccessException e) {
        last = e;
        log.warn("网易云登录代理连接失败(第{}次): {}", attempt, e.getMessage());
        JsonNode fallback = tryFallbackLoginPost(form);
        if (fallback != null) {
          return fallback;
        }
      } catch (RestClientException e) {
        last = e;
        log.warn("网易云登录代理请求失败(第{}次): {}", attempt, e.getMessage());
      } catch (Exception e) {
        log.warn("网易云登录代理响应解析失败", e);
        throw new RestClientException("parse failed", e);
      }
    }
    if (last != null) {
      throw last;
    }
    throw new RestClientException("request failed");
  }

  public JsonNode songUrl(long songId, int br, String cookieHeaderOrNull) throws RestClientException {
    String uri = UriComponentsBuilder.fromUriString("/song/url")
        .queryParam("id", songId)
        .queryParam("br", br)
        .build(true)
        .toUriString();
    return getJson(uri, cookieHeaderOrNull);
  }

  public JsonNode lyric(long songId) throws RestClientException {
    String uri = UriComponentsBuilder.fromUriString("/lyric")
        .queryParam("id", songId)
        .build(true)
        .toUriString();
    return getJson(uri, null);
  }

  /**
   * 对应 {@code GET /cloudsearch}（<a href="https://www.npmjs.com/package/@neteasecloudmusicapienhanced/api">增强 API</a>）。
   * type：1 单曲、10 专辑、100 歌手、1000 歌单。
   */
  public JsonNode cloudSearch(String keywords, int limit, int searchType) throws RestClientException {
    String uri = UriComponentsBuilder.fromUriString("/cloudsearch")
        .queryParam("keywords", keywords)
        .queryParam("limit", Math.min(Math.max(limit, 1), 100))
        .queryParam("type", searchType)
        .build(true)
        .toUriString();
    return getJson(uri, null);
  }

  public JsonNode userPlaylist(long uid, int offset, int limit, String cookieHeader) throws RestClientException {
    String uri = UriComponentsBuilder.fromUriString("/user/playlist")
        .queryParam("uid", uid)
        .queryParam("offset", offset)
        .queryParam("limit", limit)
        .build(true)
        .toUriString();
    return getJson(uri, cookieHeader);
  }

  public JsonNode likelist(long uid, String cookieHeader) throws RestClientException {
    String uri = UriComponentsBuilder.fromUriString("/likelist")
        .queryParam("uid", uid)
        .build(true)
        .toUriString();
    return getJson(uri, cookieHeader);
  }

  public JsonNode userRecord(long uid, int type, int limit, String cookieHeader) throws RestClientException {
    String uri = UriComponentsBuilder.fromUriString("/user/record")
        .queryParam("uid", uid)
        .queryParam("type", type)
        .queryParam("limit", limit)
        .build(true)
        .toUriString();
    return getJson(uri, cookieHeader);
  }

  public JsonNode songDetail(String idsCommaSeparated) throws RestClientException {
    String uri = UriComponentsBuilder.fromUriString("/song/detail")
        .queryParam("ids", idsCommaSeparated)
        .build(true)
        .toUriString();
    return getJson(uri, null);
  }

  public JsonNode playlistTrackAll(long playlistId, int limit, String cookieHeaderOrNull) throws RestClientException {
    String uri = UriComponentsBuilder.fromUriString("/playlist/track/all")
        .queryParam("id", playlistId)
        .queryParam("limit", limit)
        .build(true)
        .toUriString();
    return getJson(uri, cookieHeaderOrNull);
  }

  /** 对应第三方 API {@code GET /login/status}，用于校验 Cookie 是否已登录。 */
  public JsonNode loginStatus(String cookieHeaderOrNull) throws RestClientException {
    return getJson("/login/status", cookieHeaderOrNull);
  }

  private JsonNode getJson(String relativeUri, String cookieHeaderOrNull) {
    int maxAttempt = Math.max(1, properties.getRetryCount() + 1);
    RestClientException last = null;
    for (int attempt = 1; attempt <= maxAttempt; attempt++) {
      try {
        String body = doGetJson(restClient, relativeUri, cookieHeaderOrNull);
        if (body == null || body.isBlank()) {
          return MAPPER.createObjectNode();
        }
        return MAPPER.readTree(body);
      } catch (ResourceAccessException e) {
        last = e;
        log.warn("网易云代理连接失败(第{}次): {}", attempt, e.getMessage());
        JsonNode fallback = tryFallbackGetJson(relativeUri, cookieHeaderOrNull);
        if (fallback != null) {
          return fallback;
        }
      } catch (RestClientException e) {
        last = e;
        log.warn("网易云代理请求失败(第{}次): {}", attempt, e.getMessage());
      } catch (Exception e) {
        log.warn("网易云代理响应解析失败", e);
        throw new RestClientException("parse failed", e);
      }
    }
    if (last != null) {
      throw last;
    }
    throw new RestClientException("request failed");
  }

  private String doGetJson(RestClient client, String relativeUri, String cookieHeaderOrNull) {
    return client.get()
        .uri(relativeUri)
        .headers(h -> {
          // 某些上游会返回压缩流，JDK 客户端对该场景下字符串解码不稳定，强制返回明文 JSON
          h.set(HttpHeaders.ACCEPT_ENCODING, "identity");
          if (cookieHeaderOrNull != null && !cookieHeaderOrNull.isBlank()) {
            h.add(HttpHeaders.COOKIE, cookieHeaderOrNull);
          }
        })
        .retrieve()
        .body(String.class);
  }

  private JsonNode tryFallbackGetJson(String relativeUri, String cookieHeaderOrNull) {
    if (!canUseFallback()) {
      return null;
    }
    try {
      String body = doGetJson(fallbackRestClient, relativeUri, cookieHeaderOrNull);
      if (body == null || body.isBlank()) {
        return MAPPER.createObjectNode();
      }
      log.warn("网易云主代理不可达，已回退到备用代理: {}", relativeUri);
      return MAPPER.readTree(body);
    } catch (Exception ex) {
      log.warn("网易云备用代理请求失败: {}", ex.getMessage());
      return null;
    }
  }

  private JsonNode tryFallbackLoginPost(MultiValueMap<String, String> form) {
    if (!canUseFallback()) {
      return null;
    }
    try {
      String body = fallbackRestClient.post()
          .uri("/login/cellphone")
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(form)
          .headers(h -> h.set(HttpHeaders.ACCEPT_ENCODING, "identity"))
          .exchange((request, response) -> {
            try (response) {
              return StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
            }
          });
      if (body == null || body.isBlank()) {
        return MAPPER.createObjectNode();
      }
      log.warn("网易云主代理不可达，登录请求已回退到备用代理");
      return MAPPER.readTree(body);
    } catch (Exception ex) {
      log.warn("网易云登录备用代理请求失败: {}", ex.getMessage());
      return null;
    }
  }

  private boolean canUseFallback() {
    return properties.isLocalFallbackEnabled() && isLocalBaseUrl(primaryBaseUrl) && fallbackRestClient != null;
  }

  private static boolean isLocalBaseUrl(String baseUrl) {
    if (baseUrl == null) {
      return false;
    }
    String u = baseUrl.toLowerCase();
    return u.contains("127.0.0.1") || u.contains("localhost");
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
