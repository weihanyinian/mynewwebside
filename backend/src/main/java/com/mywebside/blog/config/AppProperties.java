package com.mywebside.blog.config;

import java.util.ArrayList;
import java.util.List;
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

  /**
   * 「每日一句」文案列表（按自然日轮转）。可通过 application.yml 的 {@code app.daily-quotes} 覆盖。
   */
  private List<String> dailyQuotes = new ArrayList<>(List.of(
      "维寒一念，步履不停。",
      "代码与文字，都是留给自己和访客的路标。",
      "今日宜：读一页书，写一行诗。"
  ));

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

  public List<String> getDailyQuotes() {
    return dailyQuotes;
  }

  public void setDailyQuotes(List<String> dailyQuotes) {
    this.dailyQuotes = dailyQuotes != null ? dailyQuotes : new ArrayList<>();
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
