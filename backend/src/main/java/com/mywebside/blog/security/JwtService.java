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
    this.key = Keys.hmacShaKeyFor(props.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
  }

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
