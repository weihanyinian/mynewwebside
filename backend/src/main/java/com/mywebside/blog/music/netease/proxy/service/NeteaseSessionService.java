package com.mywebside.blog.music.netease.proxy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.music.netease.proxy.client.NeteaseBinaryifyClient;
import com.mywebside.blog.music.netease.proxy.crypto.NeteaseSessionCrypto;
import com.mywebside.blog.music.netease.proxy.dto.NeteaseMusicDtos.NeteaseStatusDto;
import com.mywebside.blog.music.netease.proxy.persistence.NeteaseUserSessionEntity;
import com.mywebside.blog.music.netease.proxy.persistence.NeteaseUserSessionRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

@Service
public class NeteaseSessionService {

  private final NeteaseUserSessionRepository repository;
  private final NeteaseSessionCrypto crypto;
  private final NeteaseBinaryifyClient client;

  public NeteaseSessionService(
      NeteaseUserSessionRepository repository,
      NeteaseSessionCrypto crypto,
      NeteaseBinaryifyClient client
  ) {
    this.repository = repository;
    this.crypto = crypto;
    this.client = client;
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
      throw new BusinessException(502, "网易云登录服务暂时不可用");
    }
    int code = root.path("code").asInt(-1);
    if (code != 200) {
      String msg = root.path("msg").asText("");
      if (msg.isBlank()) {
        msg = root.path("message").asText("手机号或密码错误");
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
}
