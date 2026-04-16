package com.mywebside.blog.oj;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.oj.domain.Problem;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 调用 Judge0 CE：提交代码并轮询结果（base64 编解码由本类完成）。
 */
@Service
public class Judge0Service {

  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final int pollIntervalMs;
  private final int pollMaxRounds;

  public Judge0Service(
      ObjectMapper objectMapper,
      @Value("${oj.judge0.base-url:https://ce.judge0.com}") String baseUrl,
      @Value("${oj.judge0.poll-interval-ms:400}") int pollIntervalMs,
      @Value("${oj.judge0.poll-max-rounds:80}") int pollMaxRounds,
      @Value("${oj.judge0.rapid-api-key:}") String rapidApiKey,
      @Value("${oj.judge0.rapid-api-host:}") String rapidApiHost
  ) {
    this.objectMapper = objectMapper;
    this.pollIntervalMs = pollIntervalMs;
    this.pollMaxRounds = pollMaxRounds;
    String base = baseUrl.trim().replaceAll("/+$", "");
    RestClient.Builder b = RestClient.builder().baseUrl(base);
    if (rapidApiKey != null && !rapidApiKey.isBlank()) {
      b.defaultHeader("X-RapidAPI-Key", rapidApiKey.trim());
      if (rapidApiHost != null && !rapidApiHost.isBlank()) {
        b.defaultHeader("X-RapidAPI-Host", rapidApiHost.trim());
      }
    }
    this.restClient = b.build();
  }

  /** 单次提交并等待终态（队列/执行中则轮询）。 */
  public RawOutcome runOnce(Problem problem, int languageId, String sourceCode, String stdin) {
    String token = createSubmission(problem, languageId, sourceCode, stdin);
    return pollUntilDone(token);
  }

  private String createSubmission(Problem problem, int languageId, String sourceCode, String stdin) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("source_code", b64(sourceCode));
    body.put("language_id", languageId);
    body.put("stdin", b64(stdin == null ? "" : stdin));
    body.put("cpu_time_limit", problem.timeLimitSec());
    body.put("memory_limit", problem.memoryLimitMb() * 1024);

    try {
      String raw = restClient.post()
          .uri(uriBuilder -> uriBuilder
              .path("/submissions")
              .queryParam("base64_encoded", "true")
              .queryParam("wait", "false")
              .build())
          .contentType(MediaType.APPLICATION_JSON)
          .body(body)
          .retrieve()
          .body(String.class);
      JsonNode root = objectMapper.readTree(raw);
      if (!root.hasNonNull("token")) {
        throw new BusinessException(502, "Judge0 未返回 token：" + raw);
      }
      return root.get("token").asText();
    } catch (RestClientException e) {
      throw new BusinessException(502, "Judge0 请求失败：" + e.getMessage());
    } catch (Exception e) {
      throw new BusinessException(502, "Judge0 解析失败：" + e.getMessage());
    }
  }

  private RawOutcome pollUntilDone(String token) {
    try {
      for (int i = 0; i < pollMaxRounds; i++) {
        String raw = restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/submissions/" + token)
                .queryParam("base64_encoded", "true")
                .build())
            .retrieve()
            .body(String.class);
        JsonNode root = objectMapper.readTree(raw);
        int statusId = root.path("status").path("id").asInt(0);
        if (statusId > 2) {
          return parseOutcome(root);
        }
        Thread.sleep(pollIntervalMs);
      }
      throw new BusinessException(504, "Judge0 等待超时，请稍后重试");
    } catch (BusinessException e) {
      throw e;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new BusinessException(500, "判题被中断");
    } catch (RestClientException e) {
      throw new BusinessException(502, "Judge0 轮询失败：" + e.getMessage());
    } catch (Exception e) {
      throw new BusinessException(502, "Judge0 结果解析失败：" + e.getMessage());
    }
  }

  private RawOutcome parseOutcome(JsonNode root) {
    int statusId = root.path("status").path("id").asInt(-1);
    String desc = root.path("status").path("description").asText("");
    double time = parseDouble(root.path("time").asText(null));
    int mem = root.path("memory").asInt(0);
    String out = decodeMaybeB64(root, "stdout");
    String err = decodeMaybeB64(root, "stderr");
    String cmp = decodeMaybeB64(root, "compile_output");
    return new RawOutcome(statusId, desc, time, mem, out, err, cmp);
  }

  private static double parseDouble(String s) {
    if (s == null || s.isBlank()) {
      return 0d;
    }
    try {
      return Double.parseDouble(s);
    } catch (NumberFormatException e) {
      return 0d;
    }
  }

  private static String decodeMaybeB64(JsonNode root, String field) {
    if (root == null || !root.has(field) || root.get(field).isNull()) {
      return "";
    }
    String v = root.get(field).asText("");
    if (v.isBlank()) {
      return "";
    }
    try {
      byte[] bytes = Base64.getDecoder().decode(v);
      return new String(bytes, StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      return v;
    }
  }

  private static String b64(String s) {
    return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
  }

  /** Judge0 原始返回（已解码 stdout 等）。 */
  public record RawOutcome(
      int statusId,
      String statusDescription,
      double timeSeconds,
      int memoryKb,
      String stdout,
      String stderr,
      String compileOutput
  ) {}

  /** 将前端语言枚举映射为 Judge0 language_id（CE 公共实例）。 */
  public static int languageId(String lang) {
    if (lang == null) {
      throw new BusinessException(400, "语言不能为空");
    }
    return switch (lang.trim().toUpperCase()) {
      case "C" -> 50;
      case "CPP" -> 54;
      case "JAVA" -> 62;
      case "PYTHON" -> 71;
      default -> throw new BusinessException(400, "不支持的语言：" + lang);
    };
  }
}
