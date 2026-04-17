package com.mywebside.blog.auth;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.service.UserAccountService;
import jakarta.validation.Valid;
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

  public AuthController(UserAccountService userAccountService) {
    this.userAccountService = userAccountService;
  }

  @PostMapping("/register")
  public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest req) {
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
