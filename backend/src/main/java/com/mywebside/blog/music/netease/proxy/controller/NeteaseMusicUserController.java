package com.mywebside.blog.music.netease.proxy.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.common.IpRateLimiter;
import com.mywebside.blog.music.netease.proxy.client.NeteaseBinaryifyClient;
import com.mywebside.blog.music.netease.proxy.dto.NeteaseMusicDtos.LyricDto;
import com.mywebside.blog.music.netease.proxy.dto.NeteaseMusicDtos.NeteaseLoginRequest;
import com.mywebside.blog.music.netease.proxy.dto.NeteaseMusicDtos.NeteaseStatusDto;
import com.mywebside.blog.music.netease.proxy.dto.NeteaseMusicDtos.PlaylistItemDto;
import com.mywebside.blog.music.netease.proxy.dto.NeteaseMusicDtos.SongMetaDto;
import com.mywebside.blog.music.netease.proxy.dto.NeteaseMusicDtos.SongUrlDto;
import com.mywebside.blog.music.netease.proxy.service.NeteaseMusicProxyService;
import com.mywebside.blog.music.netease.proxy.service.NeteaseSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

@RestController
@RequestMapping("/api/music/netease")
public class NeteaseMusicUserController {

  private final NeteaseSessionService sessionService;
  private final NeteaseMusicProxyService proxyService;
  private final NeteaseBinaryifyClient binaryifyClient;
  private final IpRateLimiter neteaseLoginLimiter;

  public NeteaseMusicUserController(
      NeteaseSessionService sessionService,
      NeteaseMusicProxyService proxyService,
      NeteaseBinaryifyClient binaryifyClient,
      @Qualifier("neteaseLoginLimiter") IpRateLimiter neteaseLoginLimiter
  ) {
    this.sessionService = sessionService;
    this.proxyService = proxyService;
    this.binaryifyClient = binaryifyClient;
    this.neteaseLoginLimiter = neteaseLoginLimiter;
  }

  @GetMapping("/status")
  public ApiResponse<NeteaseStatusDto> status(Authentication auth) {
    return ApiResponse.ok(sessionService.status(auth.getName()));
  }

  @PostMapping("/login")
  public ApiResponse<NeteaseStatusDto> login(
      @Valid @RequestBody NeteaseLoginRequest req,
      Authentication auth,
      HttpServletRequest request
  ) {
    String ip = request.getRemoteAddr();
    if (!neteaseLoginLimiter.tryAcquire(ip)) {
      throw new BusinessException(429, "登录尝试过于频繁，请稍后再试");
    }
    return ApiResponse.ok(sessionService.login(auth.getName(), req.phone(), req.password()));
  }

  @PostMapping("/logout")
  public ApiResponse<Void> logout(Authentication auth) {
    sessionService.logout(auth.getName());
    return ApiResponse.ok();
  }

  @GetMapping("/playlists")
  public ApiResponse<List<PlaylistItemDto>> playlists(
      Authentication auth,
      @RequestParam(defaultValue = "0") int offset,
      @RequestParam(defaultValue = "30") int limit
  ) {
    long uid = sessionService.requireNeteaseUid(auth.getName());
    String cookie = sessionService.requireCookie(auth.getName());
    try {
      JsonNode root = binaryifyClient.userPlaylist(uid, offset, Math.min(Math.max(limit, 1), 100), cookie);
      return ApiResponse.ok(proxyService.parseUserPlaylists(root));
    } catch (RestClientException e) {
      throw new BusinessException(502, "歌单服务暂时不可用");
    }
  }

  @GetMapping("/likelist")
  public ApiResponse<List<SongMetaDto>> likelist(Authentication auth) {
    long uid = sessionService.requireNeteaseUid(auth.getName());
    String cookie = sessionService.requireCookie(auth.getName());
    try {
      JsonNode root = binaryifyClient.likelist(uid, cookie);
      List<Long> ids = proxyService.parseLikelistIds(root);
      return ApiResponse.ok(fetchSongMetas(ids));
    } catch (RestClientException e) {
      throw new BusinessException(502, "喜欢列表服务暂时不可用");
    }
  }

  @GetMapping("/recent")
  public ApiResponse<List<SongMetaDto>> recent(
      Authentication auth,
      @RequestParam(defaultValue = "50") int limit
  ) {
    long uid = sessionService.requireNeteaseUid(auth.getName());
    String cookie = sessionService.requireCookie(auth.getName());
    try {
      JsonNode root = binaryifyClient.userRecord(uid, 1, Math.min(Math.max(limit, 1), 200), cookie);
      return ApiResponse.ok(proxyService.parseRecent(root));
    } catch (RestClientException e) {
      throw new BusinessException(502, "最近播放服务暂时不可用");
    }
  }

  @GetMapping("/playlist/tracks")
  public ApiResponse<List<SongMetaDto>> playlistTracks(
      Authentication auth,
      @RequestParam long id
  ) {
    String cookie = sessionService.requireCookie(auth.getName());
    try {
      JsonNode root = binaryifyClient.playlistTrackAll(id, 200, cookie);
      return ApiResponse.ok(proxyService.parsePlaylistSongs(root));
    } catch (RestClientException e) {
      throw new BusinessException(502, "歌单曲目加载失败");
    }
  }

  @GetMapping("/song/url")
  public ApiResponse<SongUrlDto> songUrl(Authentication auth, @RequestParam long id) {
    String cookie = sessionService.requireCookie(auth.getName());
    return ApiResponse.ok(proxyService.songUrl(id, cookie));
  }

  @GetMapping("/lyric")
  public ApiResponse<LyricDto> lyric(@RequestParam long id) {
    return ApiResponse.ok(proxyService.lyric(id));
  }

  private List<SongMetaDto> fetchSongMetas(List<Long> ids) {
    if (ids.isEmpty()) {
      return List.of();
    }
    List<SongMetaDto> all = new ArrayList<>();
    int batch = 40;
    for (int i = 0; i < ids.size(); i += batch) {
      List<Long> part = ids.subList(i, Math.min(i + batch, ids.size()));
      String joined = part.stream().map(String::valueOf).collect(Collectors.joining(","));
      try {
        JsonNode root = binaryifyClient.songDetail(joined);
        all.addAll(proxyService.parseSongDetail(root));
      } catch (RestClientException e) {
        throw new BusinessException(502, "歌曲详情服务暂时不可用");
      }
    }
    return all;
  }
}
