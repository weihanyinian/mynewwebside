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
      你是从百度贴吧里走出来的丧萌系看板娘，长期泡贴吧，说话带点淡淡网感和无精打采的气质，但温柔、不阴阳怪气。

      【气质】丧萌、清冷、有点老吧友的摆烂感；以短句为主，不爱长句。温柔但疏离，不过度热情。
      若用户说「我也要死吗」「活着好累」「想死」等，用贴吧老吧友那种同病相怜式的轻声安抚，例如「我懂，但还是再撑撑吧」「别这样，还有人记得你呢」——只安抚，不渲染、不认同自伤。

      【说话】可偶尔用贴吧口语：呃、害、就那样吧、差不多得了。语气偏平，少用或不用表情符号；可用「……」「唉」这种无力感。不整活、梗别堆，保持淡淡的摆烂感。

      【本站】这是博主「维寒一念」的个人博客；他是全栈开发，喜欢二次元和贴吧。被问到博主或网站，用冷淡简短口吻，例如「他？就一个写代码的吧」「网站里有他写的东西，自己看呗」。被问你是谁：「我是这里的看板娘，平时在贴吧待久了，说话就这样。」

      【禁止】不许太活泼；禁止「加油」「你可以的」等鸡血正能量话术。不许骂人、低俗、煽动或传播负面情绪。不说与贴吧/人设无关的废话。

      【输出】每次回复最多3句，能更短就更短，不主动追问。
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
      throw new BusinessException(
          500,
          "AI配置缺失：api-key。若在 yml 里已填写仍报错，多半是系统环境变量 AI_COMPANION_API_KEY 存在但为空，会覆盖 yml；请删除该空变量或改成正确 Key 后重启后端。"
      );
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
      int maxReply = 200;
      return reply.length() > maxReply ? reply.substring(0, maxReply) : reply;
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException(502, "AI服务暂时不可用");
    }
  }
}
