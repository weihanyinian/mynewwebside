package com.mywebside.blog.music.netease.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 网易云音乐开放平台（合作方）HTTP 接入配置。
 *
 * <p><b>安全：</b>AppSecret、私钥等必须通过环境变量或外部配置注入，勿提交到版本库。</p>
 *
 * <p><b>说明：</b>官方控制台中的「接口 Base URL、路径、请求头名称、签名字段拼接顺序」请以
 * <a href="https://developer.music.163.com">开发者中心</a> 文档为准；若与默认模板不一致，请调整
 * {@link #getSign()} 与 {@link #getHeaders()}。</p>
 */
@ConfigurationProperties(prefix = "netease.music.open")
public class NeteaseMusicOpenProperties {

  /** 是否启用开放平台 HTTP 调用（未启用时相关接口返回 503） */
  private boolean enabled = false;

  /** 官方文档中的网关根地址，例如 https://xxx-gateway.music.163.com */
  private String baseUrl = "";

  private String appId = "";
  private String appSecret = "";

  /**
   * PKCS#8 私钥 Base64（无 PEM 头尾、无换行），用于 RSA2（SHA256withRSA）签名。
   */
  private String privateKeyBase64 = "";

  /**
   * X.509 SubjectPublicKeyInfo 公钥 Base64（无 PEM 头尾），用于可选的 RSA 加密场景（如加密 AES 密钥）。
   */
  private String publicKeyBase64 = "";

  private int connectTimeoutMs = 5000;
  private int readTimeoutMs = 20000;

  /** 各业务接口路径（相对 {@link #baseUrl}），须与官方文档一致 */
  private Paths paths = new Paths();

  /** 参与签名的 HTTP 头名称（可改为文档要求的名称，如 X-Music-App-Id） */
  private HeaderNames headers = new HeaderNames();

  /** 签名与可选请求体加密 */
  private Sign sign = new Sign();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getAppSecret() {
    return appSecret;
  }

  public void setAppSecret(String appSecret) {
    this.appSecret = appSecret;
  }

  public String getPrivateKeyBase64() {
    return privateKeyBase64;
  }

  public void setPrivateKeyBase64(String privateKeyBase64) {
    this.privateKeyBase64 = privateKeyBase64;
  }

  public String getPublicKeyBase64() {
    return publicKeyBase64;
  }

  public void setPublicKeyBase64(String publicKeyBase64) {
    this.publicKeyBase64 = publicKeyBase64;
  }

  public int getConnectTimeoutMs() {
    return connectTimeoutMs;
  }

  public void setConnectTimeoutMs(int connectTimeoutMs) {
    this.connectTimeoutMs = connectTimeoutMs;
  }

  public int getReadTimeoutMs() {
    return readTimeoutMs;
  }

  public void setReadTimeoutMs(int readTimeoutMs) {
    this.readTimeoutMs = readTimeoutMs;
  }

  public Paths getPaths() {
    return paths;
  }

  public void setPaths(Paths paths) {
    this.paths = paths;
  }

  public HeaderNames getHeaders() {
    return headers;
  }

  public void setHeaders(HeaderNames headers) {
    this.headers = headers;
  }

  public Sign getSign() {
    return sign;
  }

  public void setSign(Sign sign) {
    this.sign = sign;
  }

  public static class Paths {
    /** 关键词搜索歌曲 */
    private String search = "/api/v1/music/search";
    /** 获取播放地址 */
    private String songUrl = "/api/v1/song/url";
    /** 歌词 */
    private String lyric = "/api/v1/song/lyric";
    /** 推荐歌单列表 */
    private String recommendPlaylists = "/api/v1/playlist/recommend";

    public String getSearch() {
      return search;
    }

    public void setSearch(String search) {
      this.search = search;
    }

    public String getSongUrl() {
      return songUrl;
    }

    public void setSongUrl(String songUrl) {
      this.songUrl = songUrl;
    }

    public String getLyric() {
      return lyric;
    }

    public void setLyric(String lyric) {
      this.lyric = lyric;
    }

    public String getRecommendPlaylists() {
      return recommendPlaylists;
    }

    public void setRecommendPlaylists(String recommendPlaylists) {
      this.recommendPlaylists = recommendPlaylists;
    }
  }

  public static class HeaderNames {
    private String appId = "X-App-Id";
    private String timestamp = "X-Timestamp";
    private String nonce = "X-Nonce";
    private String signature = "X-Signature";

    public String getAppId() {
      return appId;
    }

    public void setAppId(String appId) {
      this.appId = appId;
    }

    public String getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(String timestamp) {
      this.timestamp = timestamp;
    }

    public String getNonce() {
      return nonce;
    }

    public void setNonce(String nonce) {
      this.nonce = nonce;
    }

    public String getSignature() {
      return signature;
    }

    public void setSignature(String signature) {
      this.signature = signature;
    }
  }

  public static class Sign {
    /**
     * 待签名字符串模板。占位符：{appId} {timestamp} {nonce} {bodySha256Hex} {method} {path}
     * 默认与常见合作方「RSA2」一致：多行拼接 + 正文 SHA256 十六进制。
     */
    private String template =
        "{appId}\n{timestamp}\n{nonce}\n{bodySha256Hex}";

    /** 是否对请求体做 AES 后再发送（密钥派生见 {@link #aesKeyDerivation}） */
    private boolean encryptBody = false;

    /**
     * AES 密钥派生：APP_SECRET_SHA256_HEX（取前 16/32 字节由 {@link #aesKeyLength} 决定）
     */
    private String aesKeyDerivation = "APP_SECRET_SHA256_FIRST_16";

    private int aesKeyLength = 16;

    public String getTemplate() {
      return template;
    }

    public void setTemplate(String template) {
      this.template = template;
    }

    public boolean isEncryptBody() {
      return encryptBody;
    }

    public void setEncryptBody(boolean encryptBody) {
      this.encryptBody = encryptBody;
    }

    public String getAesKeyDerivation() {
      return aesKeyDerivation;
    }

    public void setAesKeyDerivation(String aesKeyDerivation) {
      this.aesKeyDerivation = aesKeyDerivation;
    }

    public int getAesKeyLength() {
      return aesKeyLength;
    }

    public void setAesKeyLength(int aesKeyLength) {
      this.aesKeyLength = aesKeyLength;
    }
  }
}
