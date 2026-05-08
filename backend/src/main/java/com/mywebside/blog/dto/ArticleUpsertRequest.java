package com.mywebside.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ArticleUpsertRequest(
    @NotBlank @Size(max = 200) String title,
    @NotBlank @Size(max = 400) String summary,
    @NotBlank String contentMd,
    @Size(max = 500) String coverUrl,
    @NotNull String status,
    Long categoryId,
    List<Long> tagIds
) {}
