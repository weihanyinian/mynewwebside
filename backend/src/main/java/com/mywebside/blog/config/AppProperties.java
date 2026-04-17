package com.mywebside.blog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
  private final Cors cors = new Cors();
  private final Jwt jwt = new Jwt();
  /** 首次部署：若数据库中不存在 admin 用户，则用该明文密码创建（勿提交到仓库，请用环境变量注入）。 */
  private String bootstrapAdminPassword = "";
  /**
   * 为 true 时，启动时若已存在用户名为 admin 的账号，则用 {@link #bootstrapAdminPassword} 覆盖其密码。
   * 生产环境建议保持 false，仅在需要重置管理员密码时短期开启。
   */
  private boolean bootstrapAdminSyncOnStartup = false;

  public Cors getCors() {
    return cors;
  }

  public Jwt getJwt() {
    return jwt;
  }

  public String getBootstrapAdminPassword() {
    return bootstrapAdminPassword;
  }

  public void setBootstrapAdminPassword(String bootstrapAdminPassword) {
    this.bootstrapAdminPassword = bootstrapAdminPassword;
  }

  public boolean isBootstrapAdminSyncOnStartup() {
    return bootstrapAdminSyncOnStartup;
  }

  public void setBootstrapAdminSyncOnStartup(boolean bootstrapAdminSyncOnStartup) {
    this.bootstrapAdminSyncOnStartup = bootstrapAdminSyncOnStartup;
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
