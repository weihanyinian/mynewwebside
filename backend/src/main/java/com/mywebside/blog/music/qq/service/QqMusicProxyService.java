package com.mywebsite.blog.music.qq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mywebsite.blog.music.netease.proxy.dto.NeteaseMusicDtos.MusicSearchHitDto;
import com.mywebsite.blog.music.qq.client.QqMusicApiClient;
import com.mywebsite.blog.music.qq.config.QqMusicProxyProperties;
import com.mywebsite.blog.music.qq.dto.QqMusicDtos.QqLyricDto;
import com.mywebsite.blog.music.qq.dto.QqMusicDtos.QqSongMetaDto;
import com.mywebsite.blog.music.qq.dto.QqMusicDtos.QqSongUrlDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
public class QqMusicProxyService {

  private final QqMusicApiClient client;
  private final QqMusicProxyProperties qqProperties;

  public QqMusicProxyService(QqMusicApiClient client, QqMusicProxyProperties qqProperties) {
    this.client = client;
    this.qqProperties = qqProperties;
  }

  /** 巅峰榜曲目（无需用户 Cookie，与 {@link QqMusicApiClient#topList} 一致）。 */
  public List<QqSongMetaDto> hotChartTracks(int limit) {
    return topChartTracks(qqProperties.getHotChartTopId(), limit);
  }

  public List<QqSongMetaDto> topChartTracks(int topId, int limit) {
    try {
      JsonNode root = client.topList(topId, 1, limit);
      if (root.path("result").asInt(0) != 100) {
        return List.of();
      }
      JsonNode list = root.path("data").path("list");
      if (!list.isArray()) {
        return List.of();
      }
      List<QqSongMetaDto> out = new ArrayList<>();
      for (JsonNode item : list) {
        QqSongMetaDto m = parseSongItem(item);
        if (m != null) {
          out.add(m);
        }
      }
      return out;
    } catch (RestClientException e) {
      return List.of();
    }
  }

  /**
   * @param type song | artist | album | playlist
   */
  public List<MusicSearchHitDto> searchHits(String keyword, int pageNo, int pageSize, String type) {
    int t = qqSearchT(type);
    try {
      JsonNode root = client.search(keyword, pageNo, pageSize, t);
      if (root.path("result").asInt(0) != 100) {
        return List.of();
      }
      JsonNode list = root.path("data").path("list");
      if (!list.isArray()) {
        return List.of();
      }
      return switch (t) {
        case 0 -> parseQqSongHits(list);
        case 9 -> parseQqArtistHits(list);
        case 8 -> parseQqAlbumHits(list);
        case 2 -> parseQqPlaylistHits(list);
        default -> List.of();
      };
    } catch (RestClientException e) {
      return List.of();
    }
  }

  private static int qqSearchT(String raw) {
    if (raw == null || raw.isBlank()) {
      return 0;
    }
    return switch (raw.trim().toLowerCase(Locale.ROOT)) {
      case "artist", "singer" -> 9;
      case "album" -> 8;
      case "playlist", "songlist" -> 2;
      default -> 0;
    };
  }

  private static List<MusicSearchHitDto> parseQqSongHits(JsonNode list) {
    List<MusicSearchHitDto> out = new ArrayList<>();
    for (JsonNode item : list) {
      QqSongMetaDto m = parseSongItem(item);
      if (m != null) {
        out.add(new MusicSearchHitDto("song", 0L, m.songmid(), m.name(), m.artist(), m.cover()));
      }
    }
    return out;
  }

  private static List<MusicSearchHitDto> parseQqArtistHits(JsonNode list) {
    List<MusicSearchHitDto> out = new ArrayList<>();
    for (JsonNode item : list) {
      String mid = textOr(item, "singerMID", "singer_mid", "singermid", "mid");
      if (mid == null || mid.isBlank()) {
        continue;
      }
      String name = textOr(item, "singerName", "singer_name", "name");
      if (name == null) {
        name = "";
      }
      String pic = item.path("singerPic").asText("");
      if (pic.isBlank()) {
        pic = "https://y.gtimg.cn/music/photo_new/T001R300x300M000" + mid + ".jpg";
      } else if (pic.startsWith("http://")) {
        pic = "https://" + pic.substring("http://".length());
      }
      out.add(new MusicSearchHitDto("artist", 0L, mid, name, "", pic));
    }
    return out;
  }

  private static List<MusicSearchHitDto> parseQqAlbumHits(JsonNode list) {
    List<MusicSearchHitDto> out = new ArrayList<>();
    for (JsonNode item : list) {
      String mid = textOr(item, "albumMID", "album_mid", "albummid");
      if (mid == null || mid.isBlank()) {
        continue;
      }
      String name = textOr(item, "albumName", "album_name", "name");
      if (name == null) {
        name = "";
      }
      String artist = firstSingerName(item.path("singer"));
      if (artist.isBlank()) {
        artist = item.path("singer_name").asText("");
      }
      String pic = item.path("albumPic").asText("");
      if (pic.isBlank()) {
        pic = "https://y.gtimg.cn/music/photo_new/T002R300x300M000" + mid + ".jpg";
      } else if (pic.startsWith("http://")) {
        pic = "https://" + pic.substring("http://".length());
      }
      out.add(new MusicSearchHitDto("album", 0L, mid, name, artist, pic));
    }
    return out;
  }

  private static List<MusicSearchHitDto> parseQqPlaylistHits(JsonNode list) {
    List<MusicSearchHitDto> out = new ArrayList<>();
    for (JsonNode item : list) {
      String dissid = textOr(item, "dissid", "tid", "id");
      if (dissid == null || dissid.isBlank()) {
        continue;
      }
      String name = textOr(item, "dissname", "title", "name");
      if (name == null) {
        name = "";
      }
      String logo = item.path("logo").asText("");
      if (logo.startsWith("http://")) {
        logo = "https://" + logo.substring("http://".length());
      }
      int cnt = item.path("song_count").asInt(item.path("songnum").asInt(0));
      String sub = cnt > 0 ? (cnt + " 首") : "";
      long nid = 0L;
      try {
        nid = Long.parseLong(dissid);
      } catch (NumberFormatException ignored) {
        // QQ 歌单 id 可能非纯数字，仅放在 mid
      }
      out.add(new MusicSearchHitDto("playlist", nid, dissid, name, sub, logo));
    }
    return out;
  }

  public QqSongUrlDto songUrl(String songmid, String brType, String cookieHeader) {
    try {
      JsonNode root = client.songUrl(songmid, brType, cookieHeader);
      int r = root.path("result").asInt(-1);
      if (r != 100) {
        String err = root.path("errMsg").asText("获取播放链接失败");
        return new QqSongUrlDto(null, false, "QQ_" + r, err);
      }
      String url = root.path("data").asText("");
      if (url == null || url.isBlank()) {
        return new QqSongUrlDto(null, false, "NO_URL", "无播放地址，请检查是否已登录 QQ 音乐并粘贴完整 Cookie");
      }
      if (url.startsWith("http://")) {
        url = "https://" + url.substring("http://".length());
      }
      return new QqSongUrlDto(url, true, "OK", null);
    } catch (RestClientException e) {
      return new QqSongUrlDto(null, false, "UPSTREAM", "QQ 音乐接口暂时不可用");
    }
  }

  public QqLyricDto lyric(String songmid) {
    try {
      JsonNode root = client.lyric(songmid);
      if (root.path("result").asInt(0) != 100) {
        return new QqLyricDto("", "");
      }
      JsonNode data = root.path("data");
      String lrc = data.path("lyric").asText("");
      String trans = data.path("trans").asText("");
      return new QqLyricDto(lrc, trans);
    } catch (RestClientException e) {
      return new QqLyricDto("", "");
    }
  }

  private static QqSongMetaDto parseSongItem(JsonNode item) {
    String mid = textOr(item, "songmid", "mid");
    if (mid == null || mid.isBlank()) {
      return null;
    }
    String name = textOr(item, "songname", "song_name", "title");
    if (name == null) {
      name = "";
    }
    String artist = firstSingerName(item.path("singer"));
    if (artist.isBlank()) {
      artist = item.path("singer_name").asText("");
    }
    String albummid = textOr(item, "albummid", "album_mid");
    String cover = "";
    if (albummid != null && !albummid.isBlank()) {
      cover = "https://y.gtimg.cn/music/photo_new/T002R300x300M000" + albummid + ".jpg";
    }
    return new QqSongMetaDto(mid, name, artist, cover);
  }

  private static String textOr(JsonNode n, String... keys) {
    for (String k : keys) {
      if (n.hasNonNull(k)) {
        String t = n.get(k).asText("");
        if (!t.isBlank()) {
          return t;
        }
      }
    }
    return null;
  }

  private static String firstSingerName(JsonNode singer) {
    if (singer.isArray() && singer.size() > 0) {
      return singer.get(0).path("name").asText("");
    }
    return "";
  }
}
