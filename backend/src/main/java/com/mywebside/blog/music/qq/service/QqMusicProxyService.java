package com.mywebside.blog.music.qq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.mywebside.blog.music.netease.proxy.dto.NeteaseMusicDtos.MusicSearchHitDto;
import com.mywebside.blog.music.qq.client.QqMusicApiClient;
import com.mywebside.blog.music.qq.config.QqMusicProxyProperties;
import com.mywebside.blog.music.qq.dto.QqMusicDtos.QqLyricDto;
import com.mywebside.blog.music.qq.dto.QqMusicDtos.QqSongMetaDto;
import com.mywebside.blog.music.qq.dto.QqMusicDtos.QqSongUrlDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
public class QqMusicProxyService {

  private static final Logger log = LoggerFactory.getLogger(QqMusicProxyService.class);
  private static final JsonNodeFactory NF = JsonNodeFactory.instance;

  private final QqMusicApiClient client;
  private final QqMusicProxyProperties qqProperties;

  public QqMusicProxyService(QqMusicApiClient client, QqMusicProxyProperties qqProperties) {
    this.client = client;
    this.qqProperties = qqProperties;
  }

  /** 热歌榜曲目（无需用户 Cookie），对应上游 {@code GET /getRanks}。 */
  public List<QqSongMetaDto> hotChartTracks(int limit) {
    return topChartTracks(qqProperties.getHotChartTopId(), limit);
  }

  public List<QqSongMetaDto> topChartTracks(int topId, int limit) {
    try {
      JsonNode root = client.getRanks(topId, limit, 1);
      if (!qqApiOk(root)) {
        return List.of();
      }
      JsonNode payload = qqEffectiveData(root);
      JsonNode list = payload.path("songList");
      if (!list.isArray()) {
        list = payload.path("songlist");
      }
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
    int qqType = qqSearchT(type);
    try {
      Integer catZhida = (qqType == 2) ? 1 : null;
      JsonNode root = client.searchByKey(keyword, pageSize, pageNo, catZhida);
      if (!qqApiOk(root)) {
        log.debug("QQ searchByKey not ok for q={}, keys={}", keyword, root == null ? "null" : root.fieldNames());
        return maybeSmartboxSongHits(keyword, qqType, List.of());
      }
      JsonNode data = unwrapNestedSearchData(qqEffectiveData(root));
      JsonNode list = searchListForCategory(data, qqType);
      if (!list.isArray() || list.isEmpty()) {
        log.debug("QQ searchByKey empty list for q={}, qqType={}, dataKeys={}",
            keyword, qqType, data == null ? "null" : data.fieldNames());
        return maybeSmartboxSongHits(keyword, qqType, List.of());
      }
      List<MusicSearchHitDto> primary = switch (qqType) {
        case 0 -> parseQqSongHits(list);
        case 9 -> parseQqArtistHits(list);
        case 8 -> parseQqAlbumHits(list);
        case 2 -> parseQqPlaylistHits(list);
        default -> List.of();
      };
      return maybeSmartboxSongHits(keyword, qqType, primary);
    } catch (RestClientException e) {
      log.warn("QQ search upstream error for q={}: {}", keyword, e.getMessage());
      return maybeSmartboxSongHits(keyword, qqType, List.of());
    }
  }

  /** 主搜索无单曲时，用 smartbox 再试一次（兼容上游字段差异）。 */
  private List<MusicSearchHitDto> maybeSmartboxSongHits(String keyword, int qqType, List<MusicSearchHitDto> primary) {
    if (qqType != 0 || !primary.isEmpty()) {
      return primary;
    }
    try {
      JsonNode root = client.getSmartbox(keyword.trim());
      if (!qqApiOk(root)) {
        return primary;
      }
      JsonNode data = unwrapNestedSearchData(qqEffectiveData(root));
      JsonNode list = firstArray(
          listUnderCategory(data, "song"),
          pathSingleOrArray(data, "result", "song"),
          data.path("result").path("songList"),
          pathArray(data, "data", "song")
      );
      if (!list.isArray() || list.isEmpty()) {
        return primary;
      }
      return parseQqSongHits(list);
    } catch (RestClientException e) {
      log.debug("QQ smartbox fallback failed: {}", e.getMessage());
      return primary;
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

  /** song:0 / playlist:2 / album:8 / singer:9 */
  private static JsonNode searchListForCategory(JsonNode data, int qqType) {
    return switch (qqType) {
      case 0 -> listUnderCategory(data, "song");
      case 9 -> listUnderCategory(data, "singer");
      case 8 -> listUnderCategory(data, "album");
      case 2 -> firstArray(
          listUnderCategory(data, "playlist"),
          listUnderCategory(data, "diss"),
          pathArray(data, "zhida", "diss"),
          data.path("songlist").isArray() ? data.path("songlist") : NF.arrayNode()
      );
      default -> NF.arrayNode();
    };
  }

  private static JsonNode listUnderCategory(JsonNode data, String category) {
    JsonNode n = data.path(category);
    if (n.isMissingNode() || n.isNull()) {
      return NF.arrayNode();
    }
    if (n.isArray()) {
      return n;
    }
    for (String listKey : new String[] {"list", "item", "items", "songs"}) {
      JsonNode list = n.path(listKey);
      if (list.isArray()) {
        return list;
      }
    }
    JsonNode inner = n.path("data");
    if (inner.isObject()) {
      for (String listKey : new String[] {"list", "item", "items"}) {
        JsonNode list = inner.path(listKey);
        if (list.isArray()) {
          return list;
        }
      }
    }
    return NF.arrayNode();
  }

  private static JsonNode pathSingleOrArray(JsonNode data, String a, String b) {
    JsonNode x = data.path(a).path(b);
    if (x.isArray()) {
      return x;
    }
    if (x.isObject()) {
      for (String listKey : new String[] {"list", "item", "items"}) {
        JsonNode list = x.path(listKey);
        if (list.isArray()) {
          return list;
        }
      }
    }
    return NF.arrayNode();
  }

  /**
   * 部分版本上游会把 song 包在多层 {@code data} 里，这里尽量剥到含 {@code song}/{@code singer} 的一层。
   */
  private static JsonNode unwrapNestedSearchData(JsonNode node) {
    if (node == null || !node.isObject()) {
      return node == null ? NF.objectNode() : node;
    }
    JsonNode cur = node;
    for (int i = 0; i < 5; i++) {
      if (cur.has("song") || cur.has("singer") || cur.has("album") || cur.has("diss")
          || cur.has("result")) {
        return cur;
      }
      JsonNode d = cur.get("data");
      if (d == null || !d.isObject()) {
        break;
      }
      cur = d;
    }
    return node;
  }

  private static JsonNode pathArray(JsonNode data, String a, String b) {
    JsonNode x = data.path(a).path(b);
    return x.isArray() ? x : NF.arrayNode();
  }

  private static JsonNode firstArray(JsonNode... candidates) {
    for (JsonNode c : candidates) {
      if (c != null && c.isArray() && c.size() > 0) {
        return c;
      }
    }
    return NF.arrayNode();
  }

  /**
   * 旧版直连 body 多为 {@code { code:0, data:{...} }}；sansenjian/qq-music-api 常见为
   * {@code { response: <axios.data> }}，其中搜索接口常为 {@code response.data.song}。
   */
  private static JsonNode qqEffectiveData(JsonNode root) {
    if (root == null || root.isMissingNode()) {
      return NF.objectNode();
    }
    if (root.has("error")) {
      return NF.objectNode();
    }
    if (root.has("response")) {
      JsonNode resp = root.get("response");
      if (resp == null || resp.isNull() || !resp.isObject()) {
        return NF.objectNode();
      }
      if (resp.has("data") && resp.get("data").isObject()) {
        JsonNode inner = resp.get("data");
        if (inner.has("song") || inner.has("singer") || inner.has("album") || inner.has("diss")
            || inner.has("result")) {
          return inner;
        }
        JsonNode inner2 = inner.get("data");
        if (inner2 != null && inner2.isObject()
            && (inner2.has("song") || inner2.has("singer") || inner2.has("album"))) {
          return inner2;
        }
      }
      return resp;
    }
    JsonNode legacy = root.get("data");
    if (legacy != null && !legacy.isNull() && !legacy.isMissingNode()) {
      return legacy;
    }
    return NF.objectNode();
  }

  private static boolean qqApiOk(JsonNode root) {
    if (root == null || root.isMissingNode()) {
      return false;
    }
    if (root.has("error")) {
      return false;
    }
    int code = root.path("code").asInt(-999);
    if (code == 0 || code == 200) {
      return true;
    }
    if (root.has("response") && root.get("response").isObject()) {
      JsonNode r = root.get("response");
      int c2 = r.path("code").asInt(-999);
      if (c2 == 0 || c2 == 200) {
        return true;
      }
      if (r.path("retcode").asInt(999) == 0) {
        return true;
      }
      JsonNode d = r.path("data");
      if (d.isObject() && (d.has("song") || d.has("singer") || d.has("album") || d.has("result"))) {
        return true;
      }
      if (r.has("song") || r.has("singer") || r.has("album") || r.has("lyric") || r.has("playUrl")
          || r.has("result")) {
        return true;
      }
    }
    JsonNode data = root.path("data");
    if (data.isObject()) {
      if (data.has("playUrl") || data.has("url") || data.has("songList") || data.has("songlist")) {
        return true;
      }
    }
    return false;
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
        if (artist.isBlank()) {
          artist = textOr(item, "singerName", "singer_name");
          if (artist == null) {
            artist = "";
          }
        }
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
      String dissid = textOr(item, "dissid", "tid", "id", "disstid");
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
      int cnt = item.path("song_count").asInt(item.path("songnum").asInt(item.path("listen_num").asInt(0)));
      String sub = cnt > 0 ? (cnt + " 首") : "";
      long nid = 0L;
      try {
        nid = Long.parseLong(dissid);
      } catch (NumberFormatException ignored) {
        // 歌单 id 可能非纯数字
      }
      out.add(new MusicSearchHitDto("playlist", nid, dissid, name, sub, logo));
    }
    return out;
  }

  public QqSongUrlDto songUrl(String songmid, String brType, String cookieHeader) {
    try {
      JsonNode root = client.musicPlay(songmid, null, cookieHeader);
      if (!qqApiOk(root)) {
        String err = root.path("msg").asText(root.path("message").asText("获取播放链接失败"));
        return new QqSongUrlDto(null, false, "QQ_ERR", err);
      }
      JsonNode data = unwrapPlayPayload(qqEffectiveData(root));
      String url = data.path("url").asText("");
      if ((url == null || url.isBlank()) && songmid != null && !songmid.isBlank()) {
        url = data.path("playUrl").path(songmid).path("url").asText("");
      }
      if ((url == null || url.isBlank()) && songmid != null && !songmid.isBlank()) {
        url = tryBuildUrlFromMidurlinfo(data, songmid);
      }
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

  /** v2 getMusicPlay 常在 {@code data.playUrl} 下，与搜索接口嵌套层级不一致。 */
  private static JsonNode unwrapPlayPayload(JsonNode data) {
    if (data == null || !data.isObject()) {
      return data == null ? NF.objectNode() : data;
    }
    if (data.has("playUrl") || data.has("req_0")) {
      return data;
    }
    JsonNode inner = data.get("data");
    if (inner != null && inner.isObject() && (inner.has("playUrl") || inner.has("req_0"))) {
      return inner;
    }
    return data;
  }

  /** 上游未拼好 playUrl 时，从 midurlinfo + sip 拼一条可播地址。 */
  private static String tryBuildUrlFromMidurlinfo(JsonNode data, String songmid) {
    JsonNode req = data.path("req_0").path("data");
    JsonNode infos = req.path("midurlinfo");
    if (!infos.isArray()) {
      return "";
    }
    JsonNode sipArr = req.path("sip");
    String domain = "";
    if (sipArr.isArray()) {
      for (JsonNode s : sipArr) {
        if (s.isTextual()) {
          String u = s.asText("");
          if (!u.isBlank() && (u.startsWith("https://") || u.startsWith("http://"))) {
            domain = u.endsWith("/") ? u.substring(0, u.length() - 1) : u;
            if (u.startsWith("https://") && !u.contains("ws.stream")) {
              break;
            }
          }
        }
      }
    }
    if (domain.isBlank()) {
      return "";
    }
    for (JsonNode it : infos) {
      if (!songmid.equals(it.path("songmid").asText(""))) {
        continue;
      }
      String purl = it.path("purl").asText("");
      if (!purl.isBlank()) {
        return (purl.startsWith("http") ? purl : domain + (purl.startsWith("/") ? purl : "/" + purl));
      }
      String fn = it.path("filename").asText("");
      String vkey = it.path("vkey").asText("");
      if (!fn.isBlank() && !vkey.isBlank()) {
        return domain + (fn.startsWith("/") ? fn : "/" + fn) + "?vkey=" + vkey;
      }
    }
    return "";
  }

  public QqLyricDto lyric(String songmid) {
    try {
      JsonNode root = client.lyric(songmid, true);
      if (!qqApiOk(root)) {
        return new QqLyricDto("", "");
      }
      JsonNode data = qqEffectiveData(root);
      String lrc = data.path("lyric").asText("");
      String trans = data.path("trans").asText("");
      return new QqLyricDto(lrc, trans);
    } catch (RestClientException e) {
      return new QqLyricDto("", "");
    }
  }

  private static QqSongMetaDto parseSongItem(JsonNode item) {
    String mid = textOr(item, "songmid", "song_mid", "strSongMid", "strMediaMid", "strSongMid", "mid", "songMid");
    if (mid == null || mid.isBlank()) {
      return null;
    }
    String name = textOr(item, "songname", "song_name", "title", "songName", "name");
    if (name == null) {
      name = "";
    }
    String artist = firstSingerName(item.path("singer"));
    if (artist.isBlank()) {
      artist = item.path("singer_name").asText("");
    }
    if (artist.isBlank()) {
      String sn = textOr(item, "singerName", "singer_name");
      if (sn != null) {
        artist = sn;
      }
    }
    String albummid = textOr(item, "albummid", "album_mid", "albumMid");
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
