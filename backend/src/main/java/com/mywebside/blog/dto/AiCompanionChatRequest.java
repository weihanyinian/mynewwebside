package com.mywebside.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiCompanionChatRequest(@NotBlank @Size(max = 300) String message) {}
