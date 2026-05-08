package com.mywebside.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FriendLinkUpsertRequest(
    @NotBlank @Size(max = 120) String title,
    @NotBlank @Size(max = 600) String url,
    @Size(max = 500) String description,
    @Size(max = 600) String avatarUrl,
    int sortOrder
) {}
