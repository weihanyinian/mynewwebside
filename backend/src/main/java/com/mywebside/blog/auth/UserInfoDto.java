package com.mywebsite.blog.auth;

public record UserInfoDto(long id, String username, String nickname, boolean admin) {
}
