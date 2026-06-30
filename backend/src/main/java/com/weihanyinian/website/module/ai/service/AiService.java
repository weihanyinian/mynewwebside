package com.weihanyinian.website.module.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class AiService {

    private final RestClient restClient;

    @Value("${ai.companion.enabled:false}")
    private boolean enabled;

    @Value("${ai.companion.base-url:https://api.openai.com}")
    private String baseUrl;

    @Value("${ai.companion.api-key:}")
    private String apiKey;

    @Value("${ai.companion.model:gpt-3.5-turbo}")
    private String model;

    public AiService() {
        this.restClient = RestClient.builder().build();
    }

    public boolean isEnabled() {
        return enabled;
    }

    @SuppressWarnings("unchecked")
    public String chat(String message) {
        if (!enabled) {
            throw new IllegalStateException("AI 伴聊功能未启用");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("AI API key 未配置");
        }

        Map<String, Object> reqBody = Map.of(
            "model", model,
            "messages", new Object[]{
                Map.of("role", "system", "content", "你是一个友好的AI助手，名字叫小寒。用中文回答，语气轻松自然。"),
                Map.of("role", "user", "content", message)
            }
        );

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
                return msg.get("content");
            }
        }

        throw new RuntimeException("AI 响应异常");
    }
}
