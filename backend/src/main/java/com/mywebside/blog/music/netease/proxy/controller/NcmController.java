package com.mywebside.blog.music.netease.proxy.controller;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.music.netease.proxy.service.NcmService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/ncm")
public class NcmController {

  private final NcmService ncmService;

  public NcmController(NcmService ncmService) {
    this.ncmService = ncmService;
  }

  @PostMapping("/login/password")
  public ApiResponse<Map<String, Object>> loginByPassword(
      HttpSession session,
      @Valid @RequestBody PasswordLoginRequest req
  ) {
    return ApiResponse.ok(ncmService.loginByPassword(session, req.phone(), req.password()));
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
      @Valid @RequestBody CaptchaLoginRequest req
  ) {
    return ApiResponse.ok(ncmService.loginByCaptcha(session, req.phone(), req.captcha()));
  }

  @PostMapping("/login/cookie")
  public ApiResponse<Map<String, Object>> loginByCookie(
      HttpSession session,
      @Valid @RequestBody CookieLoginRequest req
  ) {
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
      @NotBlank String password
  ) {}

  public record CaptchaSendRequest(
      @NotBlank @Pattern(regexp = "^[0-9]{11}$", message = "手机号格式不正确") String phone
  ) {}

  public record CaptchaLoginRequest(
      @NotBlank @Pattern(regexp = "^[0-9]{11}$", message = "手机号格式不正确") String phone,
      @NotBlank String captcha
  ) {}

  public record CookieLoginRequest(@NotBlank String cookie) {}
}
