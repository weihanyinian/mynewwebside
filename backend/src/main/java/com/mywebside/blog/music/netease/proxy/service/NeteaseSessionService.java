package com.mywebside.blog.music.netease.proxy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.music.netease.proxy.client.NeteaseBinaryifyClient;
import com.mywebside.blog.music.netease.proxy.config.NeteaseProxyProperties;
import com.mywebside.blog.music.netease.proxy.crypto.NeteaseSessionCrypto;
import com.mywebside.blog.music.netease.proxy.dto.NeteaseMusicDtos.NeteaseStatusDto;
import com.mywebside.blog.music.netease.proxy.persistence.NeteaseUserSessionEntity;
import com.mywebside.blog.music.netease.proxy.persistence.NeteaseUserSessionRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

@Service
public class NeteaseSessionService {

  private static final Logger log = LoggerFactory.getLogger(NeteaseSessionService.class);

  private final NeteaseUserSessionRepository repository;
  private final NeteaseSessionCrypto crypto;
  private final NeteaseBinaryifyClient client;
  private final NeteaseProxyProperties proxyProperties;

  public NeteaseSessionService(
      NeteaseUserSessionRepository repository,
      NeteaseSessionCrypto crypto,
      NeteaseBinaryifyClient client,
      NeteaseProxyProperties proxyProperties
  ) {
    this.repository = repository;
    this.crypto = crypto;
    this.client = client;
    this.proxyProperties = proxyProperties;
  }

  public NeteaseStatusDto status(String siteUsername) {
    Optional<NeteaseUserSessionEntity> opt = repository.findById(siteUsername);
    if (opt.isEmpty()) {
      return new NeteaseStatusDto(false, null, null);
    }
    NeteaseUserSessionEntity e = opt.get();
    return new NeteaseStatusDto(true, e.getNeteaseUid(), e.getNeteaseNickname());
  }

  @Transactional
  public NeteaseStatusDto login(String siteUsername, String phone, String password) {
    JsonNode root;
    try {
      root = client.loginCellphone(phone, password);
    } catch (RestClientException ex) {
      log.warn("网易云登录上游异常: {}", ex.toString());
      throw new BusinessException(502, describeLoginProxyFailure(ex));
    }
    int code = root.path("code").asInt(-1);
    if (code != 200) {
      String msg = root.path("msg").asText("");
      if (msg.isBlank()) {
        msg = root.path("message").asText("");
      }
      if (msg.isBlank()) {
        msg = "登录失败（上游 code=" + code + "）";
      }
      throw new BusinessException(400, msg);
    }
    String cookie = root.path("cookie").asText("");
    if (cookie.isBlank()) {
      throw new BusinessException(502, "上游未返回登录凭证，请稍后重试");
    }
    long uid = root.path("account").path("id").asLong(0);
    if (uid == 0) {
      uid = root.path("account").path("userId").asLong(0);
    }
    String nick = root.path("account").path("nickname").asText("");

    NeteaseUserSessionEntity entity = repository.findById(siteUsername).orElseGet(NeteaseUserSessionEntity::new);
    entity.setUsername(siteUsername);
    entity.setCookieCipher(crypto.encrypt(cookie));
    entity.setNeteaseUid(uid != 0 ? uid : null);
    entity.setNeteaseNickname(nick.isBlank() ? null : nick);
    entity.setUpdatedAt(LocalDateTime.now());
    repository.save(entity);
    return new NeteaseStatusDto(true, uid != 0 ? uid : null, nick.isBlank() ? null : nick);
  }

  @Transactional
  public void logout(String siteUsername) {
    repository.deleteById(siteUsername);
  }

  public String requireCookie(String siteUsername) {
    NeteaseUserSessionEntity e = repository.findById(siteUsername)
        .orElseThrow(() -> new BusinessException(401, "请先登录网易云账号"));
    try {
      return crypto.decrypt(e.getCookieCipher());
    } catch (Exception ex) {
      throw new BusinessException(401, "网易云凭证已失效，请重新绑定");
    }
  }

  public long requireNeteaseUid(String siteUsername) {
    NeteaseUserSessionEntity e = repository.findById(siteUsername)
        .orElseThrow(() -> new BusinessException(401, "请先登录网易云账号"));
    Long uid = e.getNeteaseUid();
    if (uid == null || uid == 0) {
      throw new BusinessException(400, "网易云账号信息不完整，请重新绑定");
    }
    return uid;
  }

  private String describeLoginProxyFailure(RestClientException ex) {
    if (ex instanceof ResourceAccessException) {
      return "无法连接网易云第三方 API（当前 netease.proxy.base-url="
          + proxyProperties.getBaseUrl()
          + "）。请确认已启动 NeteaseCloudMusicApi（如 Docker：binaryify/netease_cloud_music_api 映射 3000 端口），"
          + "且后端能访问该地址；若 Spring Boot 在容器内而 API 在宿主机，请改用 host.docker.internal 等，勿仅用 127.0.0.1。";
    }
    String msg = ex.getMessage() == null ? "" : ex.getMessage();
    if (msg.contains("parse failed")) {
      return "网易云 API 返回内容无法解析为 JSON，请确认 base-url 指向 NeteaseCloudMusicApi 根地址（当前："
          + proxyProperties.getBaseUrl() + "）。";
    }
    return "网易云登录请求失败（" + msg + "）。请确认第三方 API 可用且与播放接口使用同一 base-url。";
  }
}
