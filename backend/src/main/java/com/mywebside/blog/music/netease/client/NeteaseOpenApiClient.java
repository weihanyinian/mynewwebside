package com.mywebside.blog.music.netease.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.music.netease.config.NeteaseMusicOpenProperties;
import com.mywebside.blog.music.netease.util.NeteaseCryptoUtils;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * 调用网易云音乐开放平台 HTTP 接口：构造公共头、RSA2（SHA256withRSA）签名、可选 AES 加密 Body。
 *
 * <p>签名字符串由 {@link NeteaseMusicOpenProperties.Sign#getTemplate()} 配置，占位符：
 * {appId} {timestamp} {nonce} {bodySha256Hex} {method} {path}
 */
public class NeteaseOpenApiClient {

  private static final Logger log = LoggerFactory.getLogger(NeteaseOpenApiClient.class);

  private final NeteaseMusicOpenProperties props;
  private final ObjectMapper objectMapper;
  private final RestClient restClient;

  public NeteaseOpenApiClient(
      NeteaseMusicOpenProperties props,
      ObjectMapper objectMapper,
      RestClient neteaseOpenRestClient
  ) {
    this.props = props;
    this.objectMapper = objectMapper;
    this.restClient = neteaseOpenRestClient;
  }

  /**
   * POST JSON，path 为相对路径（须与控制台文档一致）。
   */
  public JsonNode postJson(String path, Map<String, Object> body) {
    try {
      String rawJson = objectMapper.writeValueAsString(body);
      String payload = buildPayload(rawJson);
      long ts = System.currentTimeMillis();
      String nonce = UUID.randomUUID().toString().replace("-", "");
      String bodyHashHex = NeteaseCryptoUtils.sha256Hex(payload);

      String relativePath = path.startsWith("/") ? path : "/" + path;
      var pk = NeteaseCryptoUtils.loadPkcs8PrivateKeyFromBase64(props.getPrivateKeyBase64());
      String signPlain =
          buildSignPlain(
              props.getSign().getTemplate(),
              props.getAppId(),
              ts,
              nonce,
              bodyHashHex,
              "POST",
              relativePath);
      byte[] signBytes = signPlain.getBytes(StandardCharsets.UTF_8);
      String signature = NeteaseCryptoUtils.signRsa2Base64(pk, signBytes);

      var hb = props.getHeaders();
      String responseBody =
          restClient
              .post()
              .uri(relativePath)
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON)
              .header(hb.getAppId(), props.getAppId())
              .header(hb.getTimestamp(), String.valueOf(ts))
              .header(hb.getNonce(), nonce)
              .header(hb.getSignature(), signature)
              .body(payload)
              .retrieve()
              .body(String.class);

      if (responseBody == null || responseBody.isEmpty()) {
        throw new BusinessException(502, "开放平台返回空响应");
      }
      return objectMapper.readTree(responseBody);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("调用网易云开放平台失败 path={}", path, e);
      throw new BusinessException(502, "调用网易云开放平台失败: " + e.getMessage());
    }
  }

  private String buildPayload(String rawJson) throws Exception {
    if (!props.getSign().isEncryptBody()) {
      return rawJson;
    }
    byte[] keyMaterial = NeteaseCryptoUtils.sha256Bytes(props.getAppSecret());
    int len = Math.min(props.getSign().getAesKeyLength(), keyMaterial.length);
    byte[] key = new byte[len];
    System.arraycopy(keyMaterial, 0, key, 0, len);
    byte[] iv = new byte[16];
    byte[] enc =
        NeteaseCryptoUtils.aesCbcPkcs5Encrypt(rawJson.getBytes(StandardCharsets.UTF_8), key, iv);
    return objectMapper
        .createObjectNode()
        .put("cipher", Base64.getEncoder().encodeToString(enc))
        .put("alg", "AES-CBC-PKCS5")
        .toString();
  }

  static String buildSignPlain(
      String template,
      String appId,
      long timestamp,
      String nonce,
      String bodySha256Hex,
      String method,
      String path
  ) {
    return template
        .replace("{appId}", appId)
        .replace("{timestamp}", String.valueOf(timestamp))
        .replace("{nonce}", nonce)
        .replace("{bodySha256Hex}", bodySha256Hex)
        .replace("{method}", method == null ? "" : method)
        .replace("{path}", path == null ? "" : path);
  }
}
