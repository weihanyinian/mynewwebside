package com.mywebside.blog.dto;

public record PortfolioWorkPublicDto(
    long id,
    String title,
    String desc,
    String tag,
    String cover
) {}
