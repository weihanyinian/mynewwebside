package com.mywebside.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagUpsertRequest(
    @NotBlank @Size(max = 64) String name
) {}
