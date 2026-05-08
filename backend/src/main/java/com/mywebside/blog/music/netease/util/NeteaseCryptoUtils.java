package com.mywebside.blog.music.netease.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * RSA2（SHA256withRSA）签名、SHA-256 摘要、可选 AES-CBC 请求体加密。
 */
public final class NeteaseCryptoUtils {

  private NeteaseCryptoUtils() {}

  public static PrivateKey loadPkcs8PrivateKeyFromBase64(String base64) throws Exception {
    byte[] der = Base64.getDecoder().decode(sanitizeBase64(base64));
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
    return KeyFactory.getInstance("RSA").generatePrivate(spec);
  }

  public static PublicKey loadX509PublicKeyFromBase64(String base64) throws Exception {
    byte[] der = Base64.getDecoder().decode(sanitizeBase64(base64));
    X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
    return KeyFactory.getInstance("RSA").generatePublic(spec);
  }

  /** RSA PKCS#1 v1.5 加密（例如加密随机 AES 密钥后随请求上传） */
  public static byte[] rsaEncrypt(PublicKey publicKey, byte[] plain) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    return cipher.doFinal(plain);
  }

  /**
   * RSA2：SHA256withRSA，签名结果 Base64（无换行）。
   */
  public static String signRsa2Base64(PrivateKey privateKey, byte[] data)
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature sig = Signature.getInstance("SHA256withRSA");
    sig.initSign(privateKey);
    sig.update(data);
    return Base64.getEncoder().encodeToString(sig.sign());
  }

  public static String sha256Hex(String text) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
      return toHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  public static byte[] sha256Bytes(String text) {
    try {
      return MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  public static byte[] aesCbcPkcs5Encrypt(byte[] plain, byte[] key, byte[] iv) throws Exception {
    Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
    byte[] aesKey = normalizeAesKey(key);
    c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(iv));
    return c.doFinal(plain);
  }

  /** AES 密钥 16 / 24 / 32 字节 */
  static byte[] normalizeAesKey(byte[] key) {
    if (key.length >= 32) {
      return Arrays.copyOf(key, 32);
    }
    if (key.length >= 24) {
      return Arrays.copyOf(key, 24);
    }
    return Arrays.copyOf(key, 16);
  }

  private static String toHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  private static String sanitizeBase64(String raw) {
    if (raw == null) {
      return "";
    }
    return raw.replaceAll("\\s+", "");
  }
}
