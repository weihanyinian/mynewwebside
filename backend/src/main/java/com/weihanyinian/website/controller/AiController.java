package com.weihanyinian.website.controller;

import com.weihanyinian.website.common.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Value("${ai.companion.enabled:false}")
    private boolean enabled;

    private final RestClient restClient;

    @Value("${ai.companion.base-url:https://api.openai.com}")
    private String baseUrl;

    @Value("${ai.companion.api-key:}")
    private String apiKey;

    @Value("${ai.companion.model:gpt-3.5-turbo}")
    private String model;

    public AiController() {
        this.restClient = RestClient.builder().build();
    }

    @PostMapping("/chat")
    public ApiResponse<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        if (!enabled) {
            return ApiResponse.error(503, "AI 伴聊功能未启用");
        }

        String message = request.getOrDefault("message", "");

        try {
            Map<String, Object> reqBody = Map.of(
                "model", model,
                "messages", new Object[]{
                    Map.of("role", "system", "content", "你是一个友好的AI助手，名字叫小寒。用中文回答，语气轻松自然。"),
                    Map.of("role", "user", "content", message)
                }
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri(baseUrl + "/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(reqBody)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("choices")) {
                var choices = (java.util.List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    var msg = (Map<String, String>) choices.get(0).get("message");
                    String content = msg.get("content");
                    return ApiResponse.success(Map.of("reply", content));
                }
            }

            return ApiResponse.error("AI 响应异常");
        } catch (Exception e) {
            return ApiResponse.error("AI 服务异常: " + e.getMessage());
        }
    }
}
