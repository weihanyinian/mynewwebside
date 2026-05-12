package com.mywebside.blog.music.netease.proxy.service;

import com.mywebside.blog.common.BusinessException;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class NcmService {
  private static final Logger log = LoggerFactory.getLogger(NcmService.class);
  private static final String SESSION_NCM_COOKIE = "ncm_cookie";
  private static final Set<String> COOKIE_ATTR_NAMES = Set.of(
      "path", "domain", "expires", "max-age", "samesite", "secure", "httponly", "partitioned");

  private final RestClient restClient;
  private final String baseUrl;

  public NcmService(RestClient.Builder rb, @Value("${ncm.base-url:http://localhost:3000}") String baseUrl) {
    this.restClient = rb.build();
    this.baseUrl = baseUrl.replaceAll("/+$", "");
  }

  // ---- public API ----

  public Map<String, Object> loginByPassword(HttpSession session, String phone, String password, String countrycode) {
    String cc = (countrycode == null || countrycode.isBlank()) ? "86" : countrycode.trim();
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("phone", phone);
    form.add("password", password);
    form.add("countrycode", cc);
    Map<String, Object> body = postForm(session, "/login/cellphone", form, false);
    saveCookie(session, body);
    return withSessionFlag(body, session);
  }

  public Map<String, Object> sendCaptcha(HttpSession session, String phone) {
    Map<String, Object> body = get(session, "/captcha/sent?phone=" + encode(phone), false);
    return new HashMap<>(body == null ? Map.of() : body);
  }

  public Map<String, Object> loginByCaptcha(HttpSession session, String phone, String captcha, String countrycode) {
    String cc = (countrycode == null || countrycode.isBlank()) ? "86" : countrycode.trim();
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("phone", phone);
    form.add("captcha", captcha);
    form.add("countrycode", cc);
    Map<String, Object> body = postForm(session, "/login/cellphone", form, false);
    saveCookie(session, body);
    return withSessionFlag(body, session);
  }

  public Map<String, Object> loginByCookie(HttpSession session, String rawCookie) {
    if (rawCookie == null || rawCookie.isBlank()) throw new BusinessException(400, "Cookie 不能为空");
    session.setAttribute(SESSION_NCM_COOKIE, rawCookie.trim());
    Map<String, Object> body = get(session, "/login/status", true);
    saveCookie(session, body);
    return withSessionFlag(body, session);
  }

  public Map<String, Object> loginStatus(HttpSession session) {
    return getOrDefault(session, "/login/status", true);
  }

  public Map<String, Object> qrLoginKey(HttpSession session) {
    Map<String, Object> body = get(session, "/login/qr/key?t=" + System.currentTimeMillis(), false);
    saveCookie(session, body);
    return new HashMap<>(body == null ? Map.of() : body);
  }

  public Map<String, Object> qrLoginCreate(HttpSession session, String key, boolean qrimg) {
    String path = "/login/qr/create?key=" + encode(key) + "&qrimg=" + qrimg + "&t=" + System.currentTimeMillis();
    Map<String, Object> body = get(session, path, false);
    saveCookie(session, body);
    return new HashMap<>(body == null ? Map.of() : body);
  }

  public Map<String, Object> qrLoginCheck(HttpSession session, String key) {
    String path = "/login/qr/check?key=" + encode(key) + "&t=" + System.currentTimeMillis();
    Map<String, Object> body = get(session, path, false);
    saveCookie(session, body);
    Map<String, Object> out = new HashMap<>(body == null ? Map.of() : body);
    out.put("sessionCookiePresent", getCookie(session) != null);
    return out;
  }

  public Map<String, Object> testLosslessUrl(HttpSession session, long songId) {
    Map<String, Object> data = getOrDefault(session, "/song/url/v1?id=" + songId + "&level=lossless", true);
    data.put("sessionCookiePresent", getCookie(session) != null);
    return data;
  }

  // ---- HTTP helpers ----

  @SuppressWarnings("unchecked")
  private Map<String, Object> get(HttpSession session, String path, boolean requiresCookie) {
    try {
      String cookie = getCookie(session);
      if (cookie == null && requiresCookie) throw new BusinessException(401, "未登录网易云，请先完成登录");
      var req = restClient.get().uri(baseUrl + path).header("User-Agent", "MyWebsite-NCM-Proxy/2.0");
      if (cookie != null) req.header("Cookie", cookie);
      return req.retrieve().body(Map.class);
    } catch (BusinessException e) { throw e;
    } catch (Exception e) { throw new BusinessException(502, "NCM API 请求失败: " + e.getMessage()); }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getOrDefault(HttpSession session, String path, boolean requiresCookie) {
    Map<String, Object> body = get(session, path, requiresCookie);
    return body == null ? new HashMap<>() : body;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> postForm(HttpSession session, String path, MultiValueMap<String, String> form, boolean requiresCookie) {
    try {
      String cookie = getCookie(session);
      if (cookie == null && requiresCookie) throw new BusinessException(401, "未登录网易云，请先完成登录");
      var req = restClient.post().uri(baseUrl + path)
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .header("User-Agent", "MyWebsite-NCM-Proxy/2.0");
      if (cookie != null) req.header("Cookie", cookie);
      return req.body(form).retrieve().body(Map.class);
    } catch (BusinessException e) { throw e;
    } catch (Exception e) { throw new BusinessException(502, "NCM API 请求失败: " + e.getMessage()); }
  }

  // ---- cookie management ----

  private void saveCookie(HttpSession session, Map<?, ?> body) {
    if (body == null) return;
    Object c = body.get("cookie");
    if (c instanceof String s && !s.isBlank()) {
      mergeCookie(session, s.trim());
    }
  }

  private void mergeCookie(HttpSession session, String rawWithAttrs) {
    Map<String, String> merged = new LinkedHashMap<>();
    parsePairs(merged, getCookie(session));
    parsePairs(merged, rawWithAttrs);
    String header = merged.entrySet().stream()
        .map(e -> e.getKey() + "=" + e.getValue())
        .collect(Collectors.joining("; "));
    if (!header.isBlank()) session.setAttribute(SESSION_NCM_COOKIE, header);
  }

  private void parsePairs(Map<String, String> into, String raw) {
    if (raw == null || raw.isBlank()) return;
    for (String segment : raw.split(";")) {
      String s = segment.trim();
      int eq = s.indexOf('=');
      if (eq <= 0) continue;
      String name = s.substring(0, eq).trim();
      if (COOKIE_ATTR_NAMES.contains(name.toLowerCase(Locale.ROOT))) continue;
      into.put(name, s.substring(eq + 1).trim());
    }
  }

  private Map<String, Object> withSessionFlag(Map<String, Object> body, HttpSession session) {
    Map<String, Object> out = new HashMap<>(body == null ? Map.of() : body);
    out.put("sessionCookiePresent", getCookie(session) != null);
    return out;
  }

  private String getCookie(HttpSession session) {
    Object raw = session.getAttribute(SESSION_NCM_COOKIE);
    if (raw == null) return null;
    String value = String.valueOf(raw).trim();
    return value.isEmpty() ? null : value;
  }

  private static String encode(String value) {
    return URLEncoder.encode(Objects.toString(value, ""), StandardCharsets.UTF_8);
  }
}
