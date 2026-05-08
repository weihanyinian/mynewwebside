package com.mywebside.blog.music.netease.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mywebside.blog.music.netease.client.NeteaseOpenApiClient;
import com.mywebside.blog.music.netease.service.NeteaseMusicOpenService;
import java.net.http.HttpClient;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * 仅在 {@code netease.music.open.enabled=true} 时注册 HTTP 客户端与业务 Bean。
 */
@Configuration
@ConditionalOnProperty(prefix = "netease.music.open", name = "enabled", havingValue = "true")
public class NeteaseMusicOpenConfiguration {

  private static final Logger log = LoggerFactory.getLogger(NeteaseMusicOpenConfiguration.class);

  @Bean
  RestClient neteaseOpenRestClient(NeteaseMusicOpenProperties props) {
    validate(props);
    String base = props.getBaseUrl().trim();
    if (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }
    HttpClient http =
        HttpClient.newBuilder().connectTimeout(Duration.ofMillis(props.getConnectTimeoutMs())).build();
    JdkClientHttpRequestFactory rf = new JdkClientHttpRequestFactory(http);
    rf.setReadTimeout(Duration.ofMillis(props.getReadTimeoutMs()));
    log.info("网易云音乐开放平台 RestClient 已启用，baseUrl={}", base);
    return RestClient.builder().baseUrl(base).requestFactory(rf).build();
  }

  @Bean
  NeteaseOpenApiClient neteaseOpenApiClient(
      NeteaseMusicOpenProperties props,
      ObjectMapper objectMapper,
      RestClient neteaseOpenRestClient
  ) {
    return new NeteaseOpenApiClient(props, objectMapper, neteaseOpenRestClient);
  }

  @Bean
  NeteaseMusicOpenService neteaseMusicOpenService(
      NeteaseMusicOpenProperties props,
      NeteaseOpenApiClient client
  ) {
    return new NeteaseMusicOpenService(props, client);
  }

  private static void validate(NeteaseMusicOpenProperties props) {
    if (props.getBaseUrl() == null || props.getBaseUrl().isBlank()) {
      throw new IllegalStateException("netease.music.open.base-url 不能为空");
    }
    if (props.getAppId() == null || props.getAppId().isBlank()) {
      throw new IllegalStateException("netease.music.open.app-id 不能为空");
    }
    if (props.getPrivateKeyBase64() == null || props.getPrivateKeyBase64().isBlank()) {
      throw new IllegalStateException("netease.music.open.private-key-base64 不能为空");
    }
    try {
      com.mywebside.blog.music.netease.util.NeteaseCryptoUtils.loadPkcs8PrivateKeyFromBase64(
          props.getPrivateKeyBase64()
      );
    } catch (Exception e) {
      throw new IllegalStateException("私钥 Base64 无法解析为 PKCS#8 RSA 私钥", e);
    }
  }
}
