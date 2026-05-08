package com.mywebside.blog.music.netease.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mywebside.blog.music.netease.client.NeteaseOpenApiClient;
import com.mywebside.blog.music.netease.config.NeteaseMusicOpenProperties;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 网易云音乐开放平台业务封装：搜索、播放地址、歌词、推荐歌单。
 *
 * <p>请求体字段名须与官方文档一致；若不一致，请改此类中的 {@code Map.of} / {@code put} 键名。</p>
 */
public class NeteaseMusicOpenService {

  private final NeteaseMusicOpenProperties props;
  private final NeteaseOpenApiClient client;

  public NeteaseMusicOpenService(NeteaseMusicOpenProperties props, NeteaseOpenApiClient client) {
    this.props = props;
    this.client = client;
  }

  /** 关键词搜索歌曲 */
  public JsonNode searchSongs(String keyword, int limit, int offset) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("keyword", keyword);
    body.put("limit", limit);
    body.put("offset", offset);
    return client.postJson(props.getPaths().getSearch(), body);
  }

  /** 获取歌曲播放地址（文档字段一般为 songId / id / ids） */
  public JsonNode getSongUrl(long songId, int bitrate) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("id", songId);
    body.put("bitrate", bitrate);
    return client.postJson(props.getPaths().getSongUrl(), body);
  }

  /** 获取歌词 */
  public JsonNode getLyric(long songId) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("id", songId);
    return client.postJson(props.getPaths().getLyric(), body);
  }

  /** 推荐歌单 */
  public JsonNode recommendPlaylists(int limit) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("limit", limit);
    return client.postJson(props.getPaths().getRecommendPlaylists(), body);
  }
}
