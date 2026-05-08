package com.mywebside.blog.dto;

public record FriendLinkPublicDto(
    long id,
    String title,
    String url,
    String description,
    String avatarUrl
) {}
