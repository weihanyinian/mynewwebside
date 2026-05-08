package com.mywebside.blog.auth;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.common.IpRateLimiter;
import com.mywebside.blog.service.UserAccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserAccountService userAccountService;
  private final IpRateLimiter registerLimiter;

  public AuthController(
      UserAccountService userAccountService,
      @Qualifier("registerRateLimiter") IpRateLimiter registerLimiter
  ) {
    this.userAccountService = userAccountService;
    this.registerLimiter = registerLimiter;
  }

  @PostMapping("/register")
  public ApiResponse<Void> register(
      @Valid @RequestBody RegisterRequest req,
      HttpServletRequest request
  ) {
    String ip = request.getRemoteAddr();
    if (!registerLimiter.tryAcquire(ip)) {
      throw new BusinessException(429, "注册请求过于频繁，请稍后再试");
    }
    userAccountService.register(req);
    return ApiResponse.ok();
  }

  @PostMapping("/login")
  public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
    return ApiResponse.ok(userAccountService.login(req));
  }

  @GetMapping("/me")
  public ApiResponse<UserInfoDto> me(Authentication authentication) {
    var u = userAccountService.requireByUsername(authentication.getName());
    return ApiResponse.ok(userAccountService.toInfo(u));
  }
}
