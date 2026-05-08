package com.mywebside.blog.dto;

public record PortfolioWorkDetailDto(
    long id,
    String title,
    String desc,
    String detail,
    String contentMd,
    String tag,
    String link,
    String demoUrl,
    String repoUrl,
    String techStack,
    String cover
) {}
