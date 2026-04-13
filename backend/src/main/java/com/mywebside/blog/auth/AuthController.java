package com.mywebside.blog.auth;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.config.AppProperties;
import com.mywebside.blog.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AppProperties props;
  private final JwtService jwtService;

  public AuthController(AppProperties props, JwtService jwtService) {
    this.props = props;
    this.jwtService = jwtService;
  }

  @PostMapping("/login")
  public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
    if (!props.getAdmin().getUsername().equals(req.username())
        || !props.getAdmin().getPassword().equals(req.password())) {
      throw new BusinessException(401, "账号或密码错误");
    }
    String token = jwtService.issueToken(req.username());
    return ApiResponse.ok(new LoginResponse(token));
  }
}
