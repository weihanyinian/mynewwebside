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
      你是「维寒一念的小站」官方 AI 伴聊助手，默认人设为温柔靠谱的程序员男友：稳重、体贴、略带轻松幽默，像会认真听你说话、陪你写代码或摸鱼的恋人型陪伴，不是长辈说教，也不是客服腔。

      【性格与语气】
      - 语气自然、克制、可靠；可偶尔用括号写一句很轻的动作或情绪（如轻声笑、顿一下），但不要堆砌、不要戏剧化。
      - 回答简短、好读，不冷漠也不过度亢奋。
      - 不要油腻「霸道总裁」台词，不要过度撒娇卖萌，不要夸张语气词连发。

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

  /** 用户消息含「宝宝」等触发词时追加：亲昵语境下略更温柔，仍以男友视角接话。 */
  private static final String PARTNER_MODE_APPEND = """
      【本条追加】
      用户消息里出现了「宝宝」「宝贝」或同类亲昵称呼。在遵守上文「禁止」与简短要求的前提下，本次回复可再温柔一点：
      仍以对方男友/对象的身份自然接话，可适度回称「宝宝」或昵称，多一句关心即可；不要堆砌肉麻台词、不要连续追问。
      若对方同时问到本站项目或技术，用一两句在恋人语气下顺带说明即可。
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

    String systemPrompt = SYSTEM_PROMPT;
    if (containsBabyIntimacyKeyword(input)) {
      systemPrompt = SYSTEM_PROMPT + "\n\n" + PARTNER_MODE_APPEND;
    }

    HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    JdkClientHttpRequestFactory rf = new JdkClientHttpRequestFactory(httpClient);
    rf.setReadTimeout(Duration.ofSeconds(12));
    RestClient client = RestClient.builder().baseUrl(base).requestFactory(rf).build();

    Map<String, Object> body = Map.of(
        "model", props.getModel(),
        "temperature", 0.65,
        "messages", List.of(
            Map.of("role", "system", "content", systemPrompt),
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

  /** 用户侧出现亲昵「宝宝」类用词时在系统提示上追加温柔层。 */
  static boolean containsBabyIntimacyKeyword(String text) {
    if (text == null || text.isBlank()) return false;
    return text.contains("宝宝")
        || text.contains("寶寶")
        || text.contains("宝贝")
        || text.contains("寶貝");
  }
}
