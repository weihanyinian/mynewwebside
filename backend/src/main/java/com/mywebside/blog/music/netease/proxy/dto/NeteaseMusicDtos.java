package com.mywebside.blog.music.netease.proxy.dto;

import jakarta.validation.constraints.NotBlank;

public final class NeteaseMusicDtos {

  private NeteaseMusicDtos() {}

  /**
   * 与 {@code login_cellphone} 参数一致：{@code phone}、{@code password}、可选 {@code countrycode}（默认 86）。
   * 文档：<a href="https://www.npmjs.com/package/@neteasecloudmusicapienhanced/api">@neteasecloudmusicapienhanced/api</a>
   */
  public record NeteaseLoginRequest(
      @NotBlank String phone,
      @NotBlank String password,
      String countrycode
  ) {}

  /** 使用网易云 Cookie 绑定本站账号（如扫码登录成功后上游返回的 cookie 串）。 */
  public record NeteaseCookieLoginRequest(@NotBlank String cookie) {}

  public record NeteaseStatusDto(boolean bound, Long neteaseUid, String neteaseNickname) {}

  /** 与 {@link com.mywebside.blog.controller.MusicController.PlaylistTrack} 对齐字段名，便于前端复用 */
  public record SongMetaDto(long id, String name, String artist, String cover) {}

  public record SongUrlDto(String url, boolean playable, String reasonCode, String reasonMessage) {}

  public record LyricDto(String lrc, String tlyric) {}

  public record PlaylistItemDto(long id, String name, String coverUrl, int trackCount) {}

  /**
   * 统一搜索命中项（网易云 / QQ 前端共用）。
   * <ul>
   *   <li>{@code kind}: song | artist | album | playlist</li>
   *   <li>网易云单曲：{@code id} 为歌曲 id，{@code mid} 可为空</li>
   *   <li>QQ 单曲：{@code mid} 为 songmid，{@code id} 为 0</li>
   *   <li>歌手/专辑/歌单：{@code id} 为网易云数字 id；QQ 场景下 {@code mid} 存对方 id（如 dissid）</li>
   * </ul>
   */
  public record MusicSearchHitDto(String kind, long id, String mid, String title, String subtitle, String cover) {}
}
