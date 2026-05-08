package com.mywebside.blog.music.qq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.music.netease.proxy.crypto.NeteaseSessionCrypto;
import com.mywebside.blog.music.qq.client.QqMusicApiClient;
import com.mywebside.blog.music.qq.dto.QqMusicDtos.QqStatusDto;
import com.mywebside.blog.music.qq.persistence.QqUserSessionEntity;
import com.mywebside.blog.music.qq.persistence.QqUserSessionRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

@Service
public class QqSessionService {

  private static final Pattern UIN_PATTERN = Pattern.compile("(?:^|;\\s*)uin=([^;\\s]+)", Pattern.CASE_INSENSITIVE);
  private static final Pattern WXUIN_PATTERN = Pattern.compile("(?:^|;\\s*)wxuin=([^;\\s]+)", Pattern.CASE_INSENSITIVE);

  private final QqUserSessionRepository repository;
  private final NeteaseSessionCrypto crypto;
  private final QqMusicApiClient qqClient;

  public QqSessionService(
      QqUserSessionRepository repository,
      NeteaseSessionCrypto crypto,
      QqMusicApiClient qqClient
  ) {
    this.repository = repository;
    this.crypto = crypto;
    this.qqClient = qqClient;
  }

  public QqStatusDto status(String siteUsername) {
    Optional<QqUserSessionEntity> opt = repository.findById(siteUsername);
    if (opt.isEmpty()) {
      return new QqStatusDto(false, null, null);
    }
    QqUserSessionEntity e = opt.get();
    return new QqStatusDto(true, e.getQqUin(), e.getQqNickname());
  }

  @Transactional
  public QqStatusDto loginWithCookie(String siteUsername, String rawCookie) {
    String cookie = normalizeCookie(rawCookie);
    if (cookie.isBlank()) {
      throw new BusinessException(400, "Cookie 不能为空");
    }
    String uin = extractUin(cookie);
    if (uin == null || uin.isBlank()) {
      throw new BusinessException(400, "Cookie 中未找到 uin / wxuin，请从 y.qq.com 登录后复制完整 Cookie");
    }
    uin = uin.replaceAll("\\D", "");
    if (uin.isBlank()) {
      throw new BusinessException(400, "无法解析 QQ uin");
    }

    String nickname = null;
    try {
      JsonNode detail = qqClient.userDetail(uin, cookie);
      JsonNode data = detail.path("data");
      nickname = data.path("hostname").asText("");
      if (nickname.isBlank()) {
        nickname = data.path("nick").asText("");
      }
      if (nickname.isBlank()) {
        nickname = data.path("creator").path("hostname").asText("");
      }
    } catch (RestClientException ignored) {
      // 主页接口失败时仍保存 Cookie，播放可能仍可用
    }

    QqUserSessionEntity entity = repository.findById(siteUsername).orElseGet(QqUserSessionEntity::new);
    entity.setUsername(siteUsername);
    entity.setCookieCipher(crypto.encrypt(cookie));
    entity.setQqUin(uin);
    entity.setQqNickname(nickname != null && !nickname.isBlank() ? nickname : null);
    entity.setUpdatedAt(LocalDateTime.now());
    repository.save(entity);
    return new QqStatusDto(true, uin, entity.getQqNickname());
  }

  @Transactional
  public void logout(String siteUsername) {
    repository.deleteById(siteUsername);
  }

  public String requireCookie(String siteUsername) {
    QqUserSessionEntity e = repository.findById(siteUsername)
        .orElseThrow(() -> new BusinessException(401, "请先绑定 QQ 音乐 Cookie"));
    return crypto.decrypt(e.getCookieCipher());
  }

  private static String normalizeCookie(String raw) {
    if (raw == null) {
      return "";
    }
    return raw.trim().replaceFirst("^\uFEFF", "");
  }

  private static String extractUin(String cookieHeader) {
    Matcher m = UIN_PATTERN.matcher(cookieHeader);
    if (m.find()) {
      return m.group(1).trim();
    }
    Matcher w = WXUIN_PATTERN.matcher(cookieHeader);
    if (w.find()) {
      return w.group(1).trim();
    }
    return null;
  }
}
