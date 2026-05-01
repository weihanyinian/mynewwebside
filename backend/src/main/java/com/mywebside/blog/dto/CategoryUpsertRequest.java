package com.mywebsite.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryUpsertRequest(
    @NotBlank @Size(max = 64) String name
) {}
