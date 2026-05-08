package com.mywebside.blog.music.netease.proxy.crypto;

import com.mywebside.blog.config.AppProperties;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/**
 * 使用 AES-256-GCM 加密存储网易云 Cookie；密钥由 JWT secret 派生，不落库明文。
 */
@Component
public class NeteaseSessionCrypto {

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final int IV_LEN = 12;
  private static final int TAG_BITS = 128;

  private final byte[] keyBytes;

  public NeteaseSessionCrypto(AppProperties appProperties) {
    String secret = appProperties.getJwt().getSecret();
    if (secret == null || secret.length() < 16) {
      throw new IllegalStateException("app.jwt.secret 过短，无法派生网易云会话密钥");
    }
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] raw = md.digest((secret + "|netease-session-v1").getBytes(StandardCharsets.UTF_8));
      this.keyBytes = raw;
    } catch (Exception e) {
      throw new IllegalStateException("无法派生加密密钥", e);
    }
  }

  public String encrypt(String plain) {
    if (plain == null || plain.isEmpty()) {
      throw new IllegalArgumentException("empty plain");
    }
    byte[] iv = new byte[IV_LEN];
    RANDOM.nextBytes(iv);
    try {
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new GCMParameterSpec(TAG_BITS, iv));
      byte[] ct = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
      ByteBuffer buf = ByteBuffer.allocate(iv.length + ct.length);
      buf.put(iv);
      buf.put(ct);
      return Base64.getEncoder().encodeToString(buf.array());
    } catch (Exception e) {
      throw new IllegalStateException("加密失败", e);
    }
  }

  public String decrypt(String cipherText) {
    if (cipherText == null || cipherText.isEmpty()) {
      return "";
    }
    try {
      byte[] all = Base64.getDecoder().decode(cipherText);
      if (all.length < IV_LEN + 16) {
        throw new IllegalArgumentException("密文无效");
      }
      ByteBuffer buf = ByteBuffer.wrap(all);
      byte[] iv = new byte[IV_LEN];
      buf.get(iv);
      byte[] ct = new byte[buf.remaining()];
      buf.get(ct);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new GCMParameterSpec(TAG_BITS, iv));
      byte[] pt = cipher.doFinal(ct);
      return new String(pt, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new IllegalStateException("解密失败", e);
    }
  }
}
