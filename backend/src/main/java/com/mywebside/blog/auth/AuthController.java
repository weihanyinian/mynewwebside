package com.mywebside.blog.auth;

import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.config.AppProperties;
import com.mywebside.blog.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AppProperties props;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  public AuthController(AppProperties props, JwtService jwtService, PasswordEncoder passwordEncoder) {
    this.props = props;
    this.jwtService = jwtService;
    this.passwordEncoder = passwordEncoder;
  }

  @PostMapping("/login")
  public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
    // Ideally fetch from Database 'user' table where role='admin'
    // For simplicity with AppProperties configuration:
    if (!props.getAdmin().getUsername().equals(req.username())
        || !props.getAdmin().getPassword().equals(req.password())) {
      // In real scenario: passwordEncoder.matches(req.password(), user.getPassword())
      throw new BusinessException(401, "魔法通行证或结界密钥错误");
    }
    String token = jwtService.issueToken(req.username());
    return ApiResponse.ok(new LoginResponse(token));
  }
}
