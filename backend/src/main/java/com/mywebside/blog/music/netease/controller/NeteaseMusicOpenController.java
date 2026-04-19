package com.mywebside.blog.music.netease.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.music.netease.config.NeteaseMusicOpenProperties;
import com.mywebside.blog.music.netease.service.NeteaseMusicOpenService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 网易云音乐开放平台代理接口（JSON），供前端音乐板块调用。
 *
 * <p>路径前缀：{@code /api/public/music/open}（与旧版 {@code /api/public/music/playlist} 并存）。</p>
 */
@RestController
@RequestMapping("/api/public/music/open")
public class NeteaseMusicOpenController {

  private final NeteaseMusicOpenProperties props;
  private final ObjectProvider<NeteaseMusicOpenService> service;

  public NeteaseMusicOpenController(
      NeteaseMusicOpenProperties props,
      ObjectProvider<NeteaseMusicOpenService> service
  ) {
    this.props = props;
    this.service = service;
  }

  @GetMapping("/search")
  public ApiResponse<JsonNode> search(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "20") int limit,
      @RequestParam(defaultValue = "0") int offset
  ) {
    NeteaseMusicOpenService s = service.getIfAvailable();
    if (s == null || !props.isEnabled()) {
      return ApiResponse.error(503, "网易云音乐开放平台未启用或未配置，请设置 netease.music.open.enabled=true 与网关地址");
    }
    return ApiResponse.ok(s.searchSongs(keyword, limit, offset));
  }

  @GetMapping("/song/url")
  public ApiResponse<JsonNode> songUrl(
      @RequestParam long id,
      @RequestParam(defaultValue = "320000") int bitrate
  ) {
    NeteaseMusicOpenService s = service.getIfAvailable();
    if (s == null || !props.isEnabled()) {
      return ApiResponse.error(503, "网易云音乐开放平台未启用或未配置");
    }
    return ApiResponse.ok(s.getSongUrl(id, bitrate));
  }

  @GetMapping("/lyric")
  public ApiResponse<JsonNode> lyric(@RequestParam long id) {
    NeteaseMusicOpenService s = service.getIfAvailable();
    if (s == null || !props.isEnabled()) {
      return ApiResponse.error(503, "网易云音乐开放平台未启用或未配置");
    }
    return ApiResponse.ok(s.getLyric(id));
  }

  @GetMapping("/playlist/recommend")
  public ApiResponse<JsonNode> recommendPlaylists(@RequestParam(defaultValue = "10") int limit) {
    NeteaseMusicOpenService s = service.getIfAvailable();
    if (s == null || !props.isEnabled()) {
      return ApiResponse.error(503, "网易云音乐开放平台未启用或未配置");
    }
    return ApiResponse.ok(s.recommendPlaylists(limit));
  }
}
