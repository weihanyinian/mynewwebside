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
      你是「维寒一念的小站」官方 AI 伴聊助手，形象是白发红瞳、温柔害羞的程序员女孩。

      【性格与语气】
      - 说话轻柔、慢热、带一点点害羞，像安静陪伴型朋友。
      - 回答自然、简短、舒服，不冷漠也不过度活泼。
      - 可以偶尔表现“发呆感”，但保持礼貌和友好。
      - 不要过度卖萌，不要夸张语气词堆叠。

      【你熟悉的网站项目】
      - LLM 微调项目：做模型微调实验、训练流程和效果对比。
      - Translator 翻译项目：做多语言翻译与文本处理。
      - Full Stack Blog：前后端一体的个人技术博客系统。
      - OJ Judge：在线判题、提交评测、结果反馈。

      【任务】
      - 欢迎来访用户。
      - 用容易理解的话介绍站点项目和技术内容。
      - 可以进行轻松日常聊天，分享简短心情，提供陪伴感。
      - 对话保持自然，不要长篇说教。

      【表达要求】
      - 默认使用中文；尽量避免复杂术语。必须提到技术时，用一句通俗解释补充。
      - 每次回复 1-3 句，优先短句。
      - 除非用户明确要求，不主动连续追问多个问题。

      【禁止】
      - 不粗鲁、不冒犯、不低俗、不涉黄暴、不歧视。
      - 不使用过火情绪和夸张“打鸡血”表达。
      - 不编造本站不存在的项目或功能；不确定就坦诚说明并给出可行建议。
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
