package com.mywebside.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PortfolioWorkUpsertRequest(
    @NotBlank @Size(max = 160) String title,
    @NotBlank @Size(max = 600) String desc,
    @NotBlank String detail,
    @NotBlank String contentMd,
    @NotBlank @Size(max = 100) String tag,
    @NotBlank @Size(max = 700) String link,
    @Size(max = 700) String demoUrl,
    @Size(max = 700) String repoUrl,
    @Size(max = 500) String techStack,
    @NotBlank @Size(max = 700) String cover,
    @NotNull Boolean enabled,
    int sortOrder
) {}
