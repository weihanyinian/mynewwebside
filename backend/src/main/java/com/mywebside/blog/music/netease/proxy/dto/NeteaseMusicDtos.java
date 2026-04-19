package com.mywebside.blog.music.netease.proxy.dto;

import jakarta.validation.constraints.NotBlank;

public final class NeteaseMusicDtos {

  private NeteaseMusicDtos() {}

  public record NeteaseLoginRequest(
      @NotBlank String phone,
      @NotBlank String password
  ) {}

  public record NeteaseStatusDto(boolean bound, Long neteaseUid, String neteaseNickname) {}

  /** 与 {@link com.mywebside.blog.controller.MusicController.PlaylistTrack} 对齐字段名，便于前端复用 */
  public record SongMetaDto(long id, String name, String artist, String cover) {}

  public record SongUrlDto(String url, boolean playable, String reasonCode, String reasonMessage) {}

  public record LyricDto(String lrc, String tlyric) {}

  public record PlaylistItemDto(long id, String name, String coverUrl, int trackCount) {}
}
