package com.mywebsite.blog.controller;

import com.mywebsite.blog.common.ApiResponse;
import com.mywebsite.blog.music.netease.proxy.client.NeteaseBinaryifyClient;
import com.mywebsite.blog.music.netease.proxy.config.NeteaseProxyProperties;
import com.mywebsite.blog.music.netease.proxy.dto.NeteaseMusicDtos.LyricDto;
import com.mywebsite.blog.music.netease.proxy.dto.NeteaseMusicDtos.SongMetaDto;
import com.mywebsite.blog.music.netease.proxy.dto.NeteaseMusicDtos.SongUrlDto;
import com.mywebsite.blog.music.netease.proxy.service.NeteaseMusicProxyService;
import com.mywebsite.blog.music.qq.dto.QqMusicDtos.QqSongMetaDto;
import com.mywebsite.blog.music.qq.service.QqMusicProxyService;
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

  private static final int PLAYLIST_TRACK_LIMIT = 1000;

  private static final long CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(5);

  private final NeteaseBinaryifyClient neteaseBinaryifyClient;
  private final NeteaseMusicProxyService neteaseMusicProxyService;
  private final NeteaseProxyProperties neteaseProxyProperties;
  private final QqMusicProxyService qqMusicProxyService;

  private final ConcurrentHashMap<String, CachedPlaylist> playlistCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, CachedPlaylist> hotCache = new ConcurrentHashMap<>();

  public MusicController(
      NeteaseBinaryifyClient neteaseBinaryifyClient,
      NeteaseMusicProxyService neteaseMusicProxyService,
      NeteaseProxyProperties neteaseProxyProperties,
      QqMusicProxyService qqMusicProxyService
  ) {
    this.neteaseBinaryifyClient = neteaseBinaryifyClient;
    this.neteaseMusicProxyService = neteaseMusicProxyService;
    this.neteaseProxyProperties = neteaseProxyProperties;
    this.qqMusicProxyService = qqMusicProxyService;
  }

  /**
   * 获取歌单曲目列表；省略 {@code id} 时使用配置项 {@code netease.proxy.default-playlist-id}。
   * GET /api/public/music/playlist?id=&shuffle=true
   */
  /**
   * 近期热歌：{@code netease} 为云音乐热歌榜歌单；{@code qq} 为 QQ 巅峰榜（默认 topId 见配置）。
   * GET /api/public/music/hot?source=netease|qq&limit=80
   */
  @GetMapping("/hot")
  public ApiResponse<List<PlaylistTrack>> hot(
      @RequestParam String source,
      @RequestParam(defaultValue = "80") int limit
  ) {
    int lim = Math.min(Math.max(limit, 1), 200);
    String key = source + ":" + lim;
    long now = System.currentTimeMillis();
    CachedPlaylist cached = hotCache.get(key);
    if (cached != null && !cached.tracks.isEmpty() && (now - cached.time) < CACHE_TTL_MS) {
      return ApiResponse.ok(cached.tracks);
    }
    List<PlaylistTrack> tracks =
        switch (source == null ? "" : source.trim().toLowerCase()) {
          case "qq" -> loadHotQq(lim);
          case "netease" -> loadHotNetease(lim);
          default -> Collections.emptyList();
        };
    if (tracks.isEmpty()) {
      return ApiResponse.error(503, "热歌列表暂时不可用");
    }
    hotCache.put(key, new CachedPlaylist(Collections.unmodifiableList(new ArrayList<>(tracks)), now));
    return ApiResponse.ok(tracks);
  }

  @GetMapping("/playlist")
  public ApiResponse<List<PlaylistTrack>> getPlaylist(
      @RequestParam(required = false) String id,
      @RequestParam(defaultValue = "false") boolean shuffle
  ) {
    String playlistId = (id == null || id.isBlank()) ? neteaseProxyProperties.getDefaultPlaylistId() : id;
    List<PlaylistTrack> tracks = loadPlaylist(playlistId);
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
  public ApiResponse<SongUrlDto> publicSongUrl(@RequestParam long id, @RequestParam(required = false) Integer br) {
    int quality = br != null ? Math.max(64000, br) : neteaseProxyProperties.getDefaultBr();
    return ApiResponse.ok(neteaseMusicProxyService.songUrl(id, quality, null));
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
      var root = neteaseBinaryifyClient.playlistTrackAll(Long.parseLong(playlistId), PLAYLIST_TRACK_LIMIT, null);
      List<SongMetaDto> metas = neteaseMusicProxyService.parsePlaylistSongs(root);
      List<PlaylistTrack> tracks = new ArrayList<>();
      for (SongMetaDto m : metas) {
        tracks.add(new PlaylistTrack(m.id(), m.name(), m.artist(), m.cover(), null));
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

  private List<PlaylistTrack> loadHotNetease(int limit) {
    String pid = neteaseProxyProperties.getHotChartPlaylistId();
    try {
      var root = neteaseBinaryifyClient.playlistTrackAll(Long.parseLong(pid), limit, null);
      List<SongMetaDto> metas = neteaseMusicProxyService.parsePlaylistSongs(root);
      List<PlaylistTrack> tracks = new ArrayList<>();
      for (SongMetaDto m : metas) {
        tracks.add(new PlaylistTrack(m.id(), m.name(), m.artist(), m.cover(), null));
      }
      return tracks;
    } catch (RestClientException | NumberFormatException e) {
      log.warn("加载网易云热歌榜失败: {}", pid, e);
      return Collections.emptyList();
    }
  }

  private List<PlaylistTrack> loadHotQq(int limit) {
    try {
      List<QqSongMetaDto> metas = qqMusicProxyService.hotChartTracks(limit);
      List<PlaylistTrack> tracks = new ArrayList<>();
      for (QqSongMetaDto m : metas) {
        tracks.add(new PlaylistTrack(0L, m.name(), m.artist(), m.cover(), m.songmid()));
      }
      return tracks;
    } catch (Exception e) {
      log.warn("加载 QQ 热歌榜失败", e);
      return Collections.emptyList();
    }
  }

  private record CachedPlaylist(List<PlaylistTrack> tracks, long time) {}

  /**
   * 歌单曲目 DTO；{@code songmid} 非空时表示 QQ 曲库（网易云曲目为 {@code null}）。
   */
  public record PlaylistTrack(long id, String name, String artist, String cover, String songmid) {}
}
