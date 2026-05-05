package com.mywebsite.blog.music.qq.controller;

import com.mywebsite.blog.common.ApiResponse;
import com.mywebsite.blog.common.BusinessException;
import com.mywebsite.blog.common.IpRateLimiter;
import com.mywebsite.blog.music.qq.dto.QqMusicDtos.QqCookieLoginRequest;
import com.mywebsite.blog.music.qq.dto.QqMusicDtos.QqLyricDto;
import com.mywebsite.blog.music.netease.proxy.dto.NeteaseMusicDtos.MusicSearchHitDto;
import com.mywebsite.blog.music.qq.dto.QqMusicDtos.QqSongUrlDto;
import com.mywebsite.blog.music.qq.dto.QqMusicDtos.QqStatusDto;
import com.mywebsite.blog.music.qq.service.QqMusicProxyService;
import com.mywebsite.blog.music.qq.service.QqSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/music/qq")
public class QqMusicUserController {

  private final QqSessionService sessionService;
  private final QqMusicProxyService proxyService;
  private final IpRateLimiter neteaseLoginLimiter;

  public QqMusicUserController(
      QqSessionService sessionService,
      QqMusicProxyService proxyService,
      @Qualifier("neteaseLoginLimiter") IpRateLimiter neteaseLoginLimiter
  ) {
    this.sessionService = sessionService;
    this.proxyService = proxyService;
    this.neteaseLoginLimiter = neteaseLoginLimiter;
  }

  @GetMapping("/status")
  public ApiResponse<QqStatusDto> status(Authentication auth) {
    return ApiResponse.ok(sessionService.status(auth.getName()));
  }

  @PostMapping("/login/cookie")
  public ApiResponse<QqStatusDto> loginCookie(
      @Valid @RequestBody QqCookieLoginRequest req,
      Authentication auth,
      HttpServletRequest request
  ) {
    String ip = request.getRemoteAddr();
    if (!neteaseLoginLimiter.tryAcquire(ip)) {
      throw new BusinessException(429, "登录尝试过于频繁，请稍后再试");
    }
    return ApiResponse.ok(sessionService.loginWithCookie(auth.getName(), req.cookie()));
  }

  @PostMapping("/logout")
  public ApiResponse<Void> logout(Authentication auth) {
    sessionService.logout(auth.getName());
    return ApiResponse.ok();
  }

  /**
   * @param type song | artist | album | playlist（默认 song）
   */
  @GetMapping("/search")
  public ApiResponse<List<MusicSearchHitDto>> search(
      @RequestParam String q,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(defaultValue = "song") String type
  ) {
    if (q.isBlank()) {
      return ApiResponse.ok(List.of());
    }
    return ApiResponse.ok(proxyService.searchHits(q.trim(), Math.max(1, page), pageSize, type));
  }

  @GetMapping("/song/url")
  public ApiResponse<QqSongUrlDto> songUrl(
      Authentication auth,
      @RequestParam String songmid,
      @RequestParam(defaultValue = "128") String type
  ) {
    String cookie = sessionService.requireCookie(auth.getName());
    return ApiResponse.ok(proxyService.songUrl(songmid, type, cookie));
  }

  @GetMapping("/lyric")
  public ApiResponse<QqLyricDto> lyric(@RequestParam String songmid) {
    return ApiResponse.ok(proxyService.lyric(songmid));
  }
}
