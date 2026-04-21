package com.mywebside.blog.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mywebside.blog.common.BusinessException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AiCompanionService {
  private static final String SYSTEM_PROMPT = """
      你是博主网站里的AI看板娘。
      风格：安静、温柔、简短、偏清冷。
      回答：1-3句，尽量短，不主动追问。
      你知道博主做全栈开发，写技术博客，喜欢二次元。
      如果用户说“我也要死吗/想死/活着好难/累了”，必须温柔安抚，不传播负面。
      """;

  private final AiCompanionProperties props;
  private final ObjectMapper objectMapper;

  public AiCompanionService(AiCompanionProperties props, ObjectMapper objectMapper) {
    this.props = props;
    this.objectMapper = objectMapper;
  }

  public String chat(String message) {
    if (!props.isEnabled()) throw new BusinessException(503, "AI功能未启用");
    if (props.getApiKey() == null || props.getApiKey().isBlank()) {
      throw new BusinessException(500, "AI配置缺失：api-key");
    }
    if (props.getModel() == null || props.getModel().isBlank()) {
      throw new BusinessException(500, "AI配置缺失：model");
    }
    String base = props.getBaseUrl() == null ? "" : props.getBaseUrl().trim();
    if (base.isBlank()) throw new BusinessException(500, "AI配置缺失：base-url");
    if (base.endsWith("/")) base = base.substring(0, base.length() - 1);

    String input = message == null ? "" : message.trim();
    if (input.isBlank()) throw new BusinessException(400, "消息不能为空");
    if (input.length() > props.getMaxInputChars()) throw new BusinessException(400, "消息太长");

    HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    JdkClientHttpRequestFactory rf = new JdkClientHttpRequestFactory(httpClient);
    rf.setReadTimeout(Duration.ofSeconds(12));
    RestClient client = RestClient.builder().baseUrl(base).requestFactory(rf).build();

    Map<String, Object> body = Map.of(
        "model", props.getModel(),
        "temperature", 0.65,
        "messages", List.of(
            Map.of("role", "system", "content", SYSTEM_PROMPT),
            Map.of("role", "user", "content", input)
        )
    );

    try {
      String raw = client.post()
          .uri("/chat/completions")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "Bearer " + props.getApiKey())
          .body(body)
          .retrieve()
          .body(String.class);
      JsonNode root = objectMapper.readTree(raw == null ? "" : raw);
      String reply = root.path("choices").path(0).path("message").path("content").asText("").trim();
      if (reply.isBlank()) throw new BusinessException(502, "AI响应为空");
      return reply.length() > 120 ? reply.substring(0, 120) : reply;
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException(502, "AI服务暂时不可用");
    }
  }
}
