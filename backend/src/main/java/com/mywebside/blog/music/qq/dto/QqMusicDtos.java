package com.mywebside.blog.music.qq.dto;

import jakarta.validation.constraints.NotBlank;

public final class QqMusicDtos {

  private QqMusicDtos() {}

  public record QqStatusDto(boolean bound, String qqUin, String qqNickname) {}

  public record QqSongMetaDto(String songmid, String name, String artist, String cover) {}

  public record QqCookieLoginRequest(@NotBlank String cookie) {}

  public record QqSongUrlDto(String url, boolean playable, String reasonCode, String reasonMessage) {}

  public record QqLyricDto(String lrc, String tlyric) {}
}
