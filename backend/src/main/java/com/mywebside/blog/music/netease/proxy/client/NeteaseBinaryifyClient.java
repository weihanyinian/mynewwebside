package com.mywebside.blog.music.netease.proxy.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mywebside.blog.music.netease.proxy.config.NeteaseProxyProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
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
  private final NeteaseProxyProperties properties;

  public NeteaseBinaryifyClient(NeteaseProxyProperties properties) {
    this.properties = properties;
    String base = properties.getBaseUrl();
    if (base == null || base.isBlank()) {
      throw new IllegalStateException("netease.proxy.base-url 未配置");
    }
    this.restClient = RestClient.builder().baseUrl(base.endsWith("/") ? base.substring(0, base.length() - 1) : base).build();
  }

  public JsonNode loginCellphone(String phone, String password) throws RestClientException {
    String uri = UriComponentsBuilder.fromUriString("/login/cellphone")
        .queryParam("phone", phone)
        .queryParam("password", password)
        .build(true)
        .toUriString();
    return getJson(uri, null);
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

  private JsonNode getJson(String relativeUri, String cookieHeaderOrNull) {
    int maxAttempt = Math.max(1, properties.getRetryCount() + 1);
    RestClientException last = null;
    for (int attempt = 1; attempt <= maxAttempt; attempt++) {
      try {
        String body = restClient.get()
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
        if (body == null || body.isBlank()) {
          return MAPPER.createObjectNode();
        }
        return MAPPER.readTree(body);
      } catch (ResourceAccessException e) {
        last = e;
        log.warn("网易云代理连接失败(第{}次): {}", attempt, e.getMessage());
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
}
