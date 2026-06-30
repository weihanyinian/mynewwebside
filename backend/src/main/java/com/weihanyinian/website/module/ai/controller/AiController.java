package com.weihanyinian.website.module.ai.controller;

import com.weihanyinian.website.common.ApiResponse;
import com.weihanyinian.website.module.ai.service.AiService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ApiResponse<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        if (!aiService.isEnabled()) {
            return ApiResponse.error(503, "AI 伴聊功能未启用");
        }

        String message = request.getOrDefault("message", "");
        try {
            String reply = aiService.chat(message);
            return ApiResponse.success(Map.of("reply", reply));
        } catch (Exception e) {
            return ApiResponse.error("AI 服务异常: " + e.getMessage());
        }
    }
}
