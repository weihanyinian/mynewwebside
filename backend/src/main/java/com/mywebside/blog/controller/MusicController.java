package com.mywebside.blog.controller;

import com.mywebside.blog.common.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

/**
 * 音乐代理接口（REST 前缀 {@code /api/public/music}）。
 *
 * <p>后端代理网易云歌单，避免前端跨域 + API Key 暴露问题。
 * 歌单数据缓存 5 分钟，减少对第三方 API 的请求频率。</p>
 */
@RestController
@RequestMapping("/api/public/music")
public class MusicController {

  private static final Logger log = LoggerFactory.getLogger(MusicController.class);
  private static final ObjectMapper MAPPER = new ObjectMapper();

  /** 网易云歌单 API（开源 NodeJS 版） */
  private static final String NETEASE_API = "https://netease-cloud-music-api-five-roan-58.vercel.app";
  /** 默认歌单 ID */
  private static final String DEFAULT_PLAYLIST_ID = "489057279";

  /** 歌单缓存 */
  private volatile List<PlaylistTrack> cachedPlaylist = Collections.emptyList();
  private volatile long cachedPlaylistTime = 0;
  private static final long CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(5);

  private final RestClient restClient;

  public MusicController() {
    this.restClient = RestClient.builder()
        .baseUrl(NETEASE_API)
        .build();
  }

  /**
   * 获取歌单曲目列表。
   * GET /api/public/music/playlist?id=489057279&shuffle=true
   */
  @GetMapping("/playlist")
  public ApiResponse<List<PlaylistTrack>> getPlaylist(
      @RequestParam(defaultValue = DEFAULT_PLAYLIST_ID) String id,
      @RequestParam(defaultValue = "false") boolean shuffle
  ) {
    List<PlaylistTrack> tracks = loadPlaylist(id);
    if (tracks.isEmpty()) {
      return ApiResponse.error(503, "歌单加载失败，请稍后再试");
    }

    // 如果需要随机排序
    List<PlaylistTrack> result = new ArrayList<>(tracks);
    if (shuffle) {
      Collections.shuffle(result);
    }
    return ApiResponse.ok(result);
  }

  /**
   * 加载歌单（带缓存）。
   */
  private List<PlaylistTrack> loadPlaylist(String playlistId) {
    long now = System.currentTimeMillis();
    if (!cachedPlaylist.isEmpty() && (now - cachedPlaylistTime) < CACHE_TTL_MS) {
      return cachedPlaylist;
    }

    try {
      String body = restClient.get()
          .uri("/playlist/track/all?id={id}&limit=100", playlistId)
          .retrieve()
          .body(String.class);

      if (body == null) {
        log.warn("网易云歌单 API 返回空响应");
        return cachedPlaylist;
      }

      JsonNode root = MAPPER.readTree(body);
      JsonNode songs = root.get("songs");
      if (songs == null || !songs.isArray()) {
        log.warn("网易云歌单 API 返回格式异常: {}", body.substring(0, Math.min(200, body.length())));
        return cachedPlaylist;
      }

      List<PlaylistTrack> tracks = new ArrayList<>();
      for (JsonNode song : songs) {
        String name = song.path("name").asText("");
        long songId = song.path("id").asLong(0);

        // 提取歌手名
        JsonNode arNode = song.path("ar");
        String artist = "";
        if (arNode.isArray() && arNode.size() > 0) {
          artist = arNode.get(0).path("name").asText("");
        }

        // 提取封面
        String cover = song.path("al").path("picUrl").asText("");

        tracks.add(new PlaylistTrack(songId, name, artist, cover));
      }

      if (!tracks.isEmpty()) {
        cachedPlaylist = Collections.unmodifiableList(tracks);
        cachedPlaylistTime = now;
        log.info("网易云歌单加载成功: {} 首歌", tracks.size());
      }

      return tracks;
    } catch (Exception e) {
      log.error("加载网易云歌单失败", e);
      return cachedPlaylist; // 返回旧缓存
    }
  }

  /**
   * 歌单曲目 DTO。
   */
  public record PlaylistTrack(long id, String name, String artist, String cover) {}
}
