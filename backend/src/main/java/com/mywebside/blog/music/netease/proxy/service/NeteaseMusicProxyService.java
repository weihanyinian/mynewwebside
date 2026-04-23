package com.mywebside.blog.music.netease.proxy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.music.netease.proxy.config.NeteaseProxyProperties;
import com.mywebside.blog.music.netease.proxy.client.NeteaseBinaryifyClient;
import com.mywebside.blog.music.netease.proxy.dto.NeteaseMusicDtos.LyricDto;
import com.mywebside.blog.music.netease.proxy.dto.NeteaseMusicDtos.PlaylistItemDto;
import com.mywebside.blog.music.netease.proxy.dto.NeteaseMusicDtos.SongMetaDto;
import com.mywebside.blog.music.netease.proxy.dto.NeteaseMusicDtos.SongUrlDto;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
public class NeteaseMusicProxyService {

  private final NeteaseBinaryifyClient client;
  private final NeteaseProxyProperties properties;

  public NeteaseMusicProxyService(NeteaseBinaryifyClient client, NeteaseProxyProperties properties) {
    this.client = client;
    this.properties = properties;
  }

  public SongUrlDto songUrl(long songId, String cookieHeaderOrNull) {
    return songUrl(songId, properties.getDefaultBr(), cookieHeaderOrNull);
  }

  public SongUrlDto songUrl(long songId, int br, String cookieHeaderOrNull) {
    try {
      JsonNode root = client.songUrl(songId, br, cookieHeaderOrNull);
      return parseSongUrl(root);
    } catch (RestClientException e) {
      return new SongUrlDto(null, false, "UPSTREAM", "播放服务暂时不可用");
    }
  }

  public LyricDto lyric(long songId) {
    try {
      JsonNode root = client.lyric(songId);
      int code = root.path("code").asInt(-1);
      if (code != 200) {
        return new LyricDto("", "");
      }
      String lrc = root.path("lrc").path("lyric").asText("");
      String tlyric = root.path("tlyric").path("lyric").asText("");
      return new LyricDto(lrc, tlyric);
    } catch (RestClientException e) {
      return new LyricDto("", "");
    }
  }

  public List<PlaylistItemDto> parseUserPlaylists(JsonNode root) {
    int code = root.path("code").asInt(-1);
    if (code != 200) {
      throw new BusinessException(400, root.path("msg").asText("获取歌单失败"));
    }
    JsonNode pl = root.path("playlist");
    if (!pl.isArray()) {
      return List.of();
    }
    List<PlaylistItemDto> out = new ArrayList<>();
    for (JsonNode item : pl) {
      long id = item.path("id").asLong(0);
      if (id == 0) {
        continue;
      }
      String name = item.path("name").asText("");
      String cover = item.path("coverImgUrl").asText("");
      int tc = item.path("trackCount").asInt(0);
      out.add(new PlaylistItemDto(id, name, cover, tc));
    }
    return out;
  }

  public List<Long> parseLikelistIds(JsonNode root) {
    int code = root.path("code").asInt(-1);
    if (code != 200) {
      throw new BusinessException(400, root.path("msg").asText("获取喜欢列表失败"));
    }
    JsonNode ids = root.path("ids");
    if (!ids.isArray()) {
      return List.of();
    }
    List<Long> out = new ArrayList<>();
    for (JsonNode n : ids) {
      if (n.isNumber()) {
        out.add(n.longValue());
      }
    }
    return out;
  }

  public List<SongMetaDto> parsePlaylistSongs(JsonNode root) {
    JsonNode songs = root.path("songs");
    if (!songs.isArray()) {
      return List.of();
    }
    List<SongMetaDto> out = new ArrayList<>();
    for (JsonNode song : songs) {
      long id = song.path("id").asLong(0);
      if (id == 0) {
        continue;
      }
      String name = song.path("name").asText("");
      String artist = firstArtistName(song.path("ar"));
      String cover = song.path("al").path("picUrl").asText("");
      out.add(new SongMetaDto(id, name, artist, cover));
    }
    return out;
  }

  public List<SongMetaDto> parseSongDetail(JsonNode root) {
    int code = root.path("code").asInt(-1);
    if (code != 200) {
      throw new BusinessException(502, root.path("msg").asText("歌曲详情服务暂时不可用"));
    }
    return parsePlaylistSongs(root);
  }

  public List<SongMetaDto> parseRecent(JsonNode root) {
    int code = root.path("code").asInt(-1);
    if (code != 200) {
      throw new BusinessException(400, root.path("msg").asText("获取最近播放失败"));
    }
    JsonNode list = root.path("data").path("allData");
    if (!list.isArray()) {
      list = root.path("allData");
    }
    if (!list.isArray()) {
      return List.of();
    }
    List<SongMetaDto> out = new ArrayList<>();
    for (JsonNode row : list) {
      JsonNode song = row.path("song");
      if (song.isMissingNode() || song.isNull()) {
        song = row;
      }
      long id = song.path("id").asLong(0);
      if (id == 0) {
        continue;
      }
      String name = song.path("name").asText("");
      String artist = firstArtistName(song.path("ar"));
      String cover = song.path("al").path("picUrl").asText("");
      out.add(new SongMetaDto(id, name, artist, cover));
    }
    return out;
  }

  private static String firstArtistName(JsonNode ar) {
    if (ar.isArray() && ar.size() > 0) {
      return ar.get(0).path("name").asText("");
    }
    return "";
  }

  public static SongUrlDto parseSongUrl(JsonNode root) {
    int code = root.path("code").asInt(-1);
    if (code != 200) {
      String msg = root.path("message").asText("");
      if (msg.isBlank()) {
        msg = root.path("msg").asText("获取播放链接失败");
      }
      return new SongUrlDto(null, false, "UPSTREAM_" + code, msg);
    }
    JsonNode data = root.get("data");
    if (data == null) {
      return new SongUrlDto(null, false, "NO_DATA", "无播放数据");
    }
    JsonNode first = data.isArray() && data.size() > 0 ? data.get(0) : data;
    String url = first.path("url").asText(null);
    if (url == null || url.isBlank()) {
      return new SongUrlDto(null, false, "NO_COPYRIGHT", "无版权或需登录后播放");
    }
    return new SongUrlDto(url, true, "OK", null);
  }
}
