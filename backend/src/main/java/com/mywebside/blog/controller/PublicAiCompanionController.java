package com.mywebside.blog.controller;

import com.mywebside.blog.ai.AiCompanionService;
import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.dto.AiCompanionChatRequest;
import com.mywebside.blog.dto.AiCompanionChatResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/ai/companion")
public class PublicAiCompanionController {
  private final AiCompanionService aiCompanionService;

  public PublicAiCompanionController(AiCompanionService aiCompanionService) {
    this.aiCompanionService = aiCompanionService;
  }

  @PostMapping("/chat")
  public ApiResponse<AiCompanionChatResponse> chat(@Valid @RequestBody AiCompanionChatRequest req) {
    return ApiResponse.ok(new AiCompanionChatResponse(aiCompanionService.chat(req.message())));
  }
}
