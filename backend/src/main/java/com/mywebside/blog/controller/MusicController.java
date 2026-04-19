package com.mywebside.blog.controller;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.music.netease.proxy.client.NeteaseBinaryifyClient;
import com.mywebside.blog.music.netease.proxy.dto.NeteaseMusicDtos.LyricDto;
import com.mywebside.blog.music.netease.proxy.dto.NeteaseMusicDtos.SongMetaDto;
import com.mywebside.blog.music.netease.proxy.dto.NeteaseMusicDtos.SongUrlDto;
import com.mywebside.blog.music.netease.proxy.service.NeteaseMusicProxyService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

/**
 * 音乐代理接口（REST 前缀 {@code /api/public/music}）。
 *
 * <p>后端代理网易云歌单与播放能力，避免前端跨域；歌单按 ID 缓存 5 分钟。</p>
 */
@RestController
@RequestMapping("/api/public/music")
public class MusicController {

  private static final Logger log = LoggerFactory.getLogger(MusicController.class);

  /** 默认歌单 ID */
  private static final String DEFAULT_PLAYLIST_ID = "489057279";

  private static final long CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(5);

  private final NeteaseBinaryifyClient neteaseBinaryifyClient;
  private final NeteaseMusicProxyService neteaseMusicProxyService;

  private final ConcurrentHashMap<String, CachedPlaylist> playlistCache = new ConcurrentHashMap<>();

  public MusicController(NeteaseBinaryifyClient neteaseBinaryifyClient, NeteaseMusicProxyService neteaseMusicProxyService) {
    this.neteaseBinaryifyClient = neteaseBinaryifyClient;
    this.neteaseMusicProxyService = neteaseMusicProxyService;
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
    List<PlaylistTrack> result = new ArrayList<>(tracks);
    if (shuffle) {
      Collections.shuffle(result);
    }
    return ApiResponse.ok(result);
  }

  /**
   * 未携带网易云登录态时获取播放链接（VIP / 版权受限曲目可能无法播放）。
   */
  @GetMapping("/song/url")
  public ApiResponse<SongUrlDto> publicSongUrl(@RequestParam long id) {
    return ApiResponse.ok(neteaseMusicProxyService.songUrl(id, null));
  }

  @GetMapping("/lyric")
  public ApiResponse<LyricDto> publicLyric(@RequestParam long id) {
    return ApiResponse.ok(neteaseMusicProxyService.lyric(id));
  }

  private List<PlaylistTrack> loadPlaylist(String playlistId) {
    long now = System.currentTimeMillis();
    CachedPlaylist cached = playlistCache.get(playlistId);
    if (cached != null && !cached.tracks.isEmpty() && (now - cached.time) < CACHE_TTL_MS) {
      return cached.tracks;
    }
    try {
      var root = neteaseBinaryifyClient.playlistTrackAll(Long.parseLong(playlistId), 100, null);
      List<SongMetaDto> metas = neteaseMusicProxyService.parsePlaylistSongs(root);
      List<PlaylistTrack> tracks = new ArrayList<>();
      for (SongMetaDto m : metas) {
        tracks.add(new PlaylistTrack(m.id(), m.name(), m.artist(), m.cover()));
      }
      if (!tracks.isEmpty()) {
        playlistCache.put(playlistId, new CachedPlaylist(Collections.unmodifiableList(tracks), now));
        log.info("网易云歌单加载成功: playlistId={} size={}", playlistId, tracks.size());
      }
      return tracks;
    } catch (RestClientException | NumberFormatException e) {
      log.error("加载网易云歌单失败: {}", playlistId, e);
      if (cached != null && !cached.tracks.isEmpty()) {
        return cached.tracks;
      }
      return Collections.emptyList();
    }
  }

  private record CachedPlaylist(List<PlaylistTrack> tracks, long time) {}

  /**
   * 歌单曲目 DTO。
   */
  public record PlaylistTrack(long id, String name, String artist, String cover) {}
}
