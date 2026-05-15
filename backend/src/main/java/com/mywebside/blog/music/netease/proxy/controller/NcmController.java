package com.mywebside.blog.music.netease.proxy.controller;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.common.IpRateLimiter;
import com.mywebside.blog.music.netease.proxy.service.NcmService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 直连 api-enhanced 服务（默认 http://127.0.0.1:3000），供扫码登录等使用 HttpSession 存 Cookie。
 * 源码与部署：<a href="https://github.com/NeteaseCloudMusicApiEnhanced/api-enhanced">api-enhanced</a>；
 * npm：<a href="https://www.npmjs.com/package/@neteasecloudmusicapienhanced/api">@neteasecloudmusicapienhanced/api</a>
 */
@Validated
@RestController
@RequestMapping("/api/ncm")
public class NcmController {

  private final NcmService ncmService;
  private final IpRateLimiter neteaseLoginLimiter;

  public NcmController(
      NcmService ncmService,
      @Qualifier("neteaseLoginLimiter") IpRateLimiter neteaseLoginLimiter
  ) {
    this.ncmService = ncmService;
    this.neteaseLoginLimiter = neteaseLoginLimiter;
  }

  @PostMapping("/login/password")
  public ApiResponse<Map<String, Object>> loginByPassword(
      HttpSession session,
      HttpServletRequest request,
      @Valid @RequestBody PasswordLoginRequest req
  ) {
    if (!neteaseLoginLimiter.tryAcquire(request.getRemoteAddr())) {
      throw new BusinessException(429, "登录尝试过于频繁，请稍后再试");
    }
    return ApiResponse.ok(ncmService.loginByPassword(session, req.phone(), req.password(), req.countrycode()));
  }

  @PostMapping("/captcha/send")
  public ApiResponse<Map<String, Object>> sendCaptcha(
      HttpSession session,
      @Valid @RequestBody CaptchaSendRequest req
  ) {
    return ApiResponse.ok(ncmService.sendCaptcha(session, req.phone()));
  }

  @PostMapping("/login/captcha")
  public ApiResponse<Map<String, Object>> loginByCaptcha(
      HttpSession session,
      HttpServletRequest request,
      @Valid @RequestBody CaptchaLoginRequest req
  ) {
    if (!neteaseLoginLimiter.tryAcquire(request.getRemoteAddr())) {
      throw new BusinessException(429, "登录尝试过于频繁，请稍后再试");
    }
    return ApiResponse.ok(ncmService.loginByCaptcha(session, req.phone(), req.captcha(), req.countrycode()));
  }

  @GetMapping("/login/qr/key")
  public ApiResponse<Map<String, Object>> qrLoginKey(
      HttpSession session,
      HttpServletRequest request
  ) {
    if (!neteaseLoginLimiter.tryAcquire(request.getRemoteAddr())) {
      throw new BusinessException(429, "登录尝试过于频繁，请稍后再试");
    }
    return ApiResponse.ok(ncmService.qrLoginKey(session));
  }

  @GetMapping("/login/qr/create")
  public ApiResponse<Map<String, Object>> qrLoginCreate(
      HttpSession session,
      @RequestParam("key") @NotBlank(message = "二维码 key 不能为空") String key,
      @RequestParam(value = "qrimg", defaultValue = "true") boolean qrimg
  ) {
    return ApiResponse.ok(ncmService.qrLoginCreate(session, key, qrimg));
  }

  @GetMapping("/login/qr/check")
  public ApiResponse<Map<String, Object>> qrLoginCheck(
      HttpSession session,
      @RequestParam("key") @NotBlank(message = "二维码 key 不能为空") String key
  ) {
    return ApiResponse.ok(ncmService.qrLoginCheck(session, key));
  }

  @PostMapping("/login/cookie")
  public ApiResponse<Map<String, Object>> loginByCookie(
      HttpSession session,
      HttpServletRequest request,
      @Valid @RequestBody CookieLoginRequest req
  ) {
    if (!neteaseLoginLimiter.tryAcquire(request.getRemoteAddr())) {
      throw new BusinessException(429, "登录尝试过于频繁，请稍后再试");
    }
    return ApiResponse.ok(ncmService.loginByCookie(session, req.cookie()));
  }

  @GetMapping("/me")
  public ApiResponse<Map<String, Object>> me(HttpSession session) {
    return ApiResponse.ok(ncmService.loginStatus(session));
  }

  @GetMapping("/test/lossless-url")
  public ApiResponse<Map<String, Object>> testLosslessUrl(
      HttpSession session,
      @RequestParam("songId") long songId
  ) {
    return ApiResponse.ok(ncmService.testLosslessUrl(session, songId));
  }

  public record PasswordLoginRequest(
      @NotBlank @Pattern(regexp = "^[0-9]{11}$", message = "手机号格式不正确") String phone,
      @NotBlank String password,
      String countrycode
  ) {}

  public record CaptchaSendRequest(
      @NotBlank @Pattern(regexp = "^[0-9]{11}$", message = "手机号格式不正确") String phone
  ) {}

  public record CaptchaLoginRequest(
      @NotBlank @Pattern(regexp = "^[0-9]{11}$", message = "手机号格式不正确") String phone,
      @NotBlank String captcha,
      String countrycode
  ) {}

  public record CookieLoginRequest(@NotBlank String cookie) {}
}
