package com.mywebside.blog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
  private final Cors cors = new Cors();
  private final Admin admin = new Admin();
  private final Jwt jwt = new Jwt();

  public Cors getCors() {
    return cors;
  }

  public Admin getAdmin() {
    return admin;
  }

  public Jwt getJwt() {
    return jwt;
  }

  public static class Cors {
    private String allowedOrigins;

    public String getAllowedOrigins() {
      return allowedOrigins;
    }

    public void setAllowedOrigins(String allowedOrigins) {
      this.allowedOrigins = allowedOrigins;
    }
  }

  public static class Admin {
    private String username;
    private String password;

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }
  }

  public static class Jwt {
    private String secret;
    private long expireMinutes;

    public String getSecret() {
      return secret;
    }

    public void setSecret(String secret) {
      this.secret = secret;
    }

    public long getExpireMinutes() {
      return expireMinutes;
    }

    public void setExpireMinutes(long expireMinutes) {
      this.expireMinutes = expireMinutes;
    }
  }
}
