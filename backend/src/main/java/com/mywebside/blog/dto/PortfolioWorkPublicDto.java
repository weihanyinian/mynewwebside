package com.mywebside.blog.dto;

public record PortfolioWorkPublicDto(
    long id,
    String title,
    String desc,
    String detail,
    String tag,
    String link,
    String cover
) {}
