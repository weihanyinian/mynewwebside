package com.mywebside.blog.auth;

public record LoginResponse(String token, String username, String nickname, boolean admin) {}
