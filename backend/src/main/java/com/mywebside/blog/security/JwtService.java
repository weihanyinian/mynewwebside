package com.mywebside.blog.security;

import com.mywebside.blog.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtService {
  private final AppProperties props;
  private final SecretKey key;

  public JwtService(AppProperties props) {
    this.props = props;
    String secret = props.getJwt().getSecret();
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("未配置 JWT 密钥：请通过环境变量 JWT_SECRET 注入（勿写入仓库）");
    }
    if (secret.length() < 32) {
      throw new IllegalStateException("JWT_SECRET 长度至少 32 字符以满足 HS256 安全建议");
    }
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  /** 令牌仅携带用户名；是否管理员由服务端根据用户名判定，避免与 JWT claim 不一致。 */
  public String issueToken(String username) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(props.getJwt().getExpireMinutes() * 60);
    return Jwts.builder()
        .subject(username)
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(key)
        .compact();
  }

  public String parseUsername(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
    return claims.getSubject();
  }
}
