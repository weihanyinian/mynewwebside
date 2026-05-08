package com.mywebside.blog.auth;

public record UserInfoDto(long id, String username, String nickname, boolean admin) {
}
