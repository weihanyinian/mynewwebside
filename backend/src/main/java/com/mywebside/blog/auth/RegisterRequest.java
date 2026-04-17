package com.mywebside.blog.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Size(min = 3, max = 64) String username,
    @NotBlank @Size(min = 6, max = 72) String password,
    @NotBlank @Size(max = 64) String nickname
) {
}
