package com.mywebside.blog.music.netease.proxy.service;

import com.mywebside.blog.common.BusinessException;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class NcmService {

  private static final String SESSION_NCM_COOKIE = "ncm_cookie";
  /** Set-Cookie / document.cookie 风格字符串里的属性名，不应作为请求 Cookie 头发送 */
  private static final Set<String> COOKIE_ATTR_NAMES = Set.of(
      "path", "domain", "expires", "max-age", "samesite", "secure", "httponly", "partitioned");
  private final RestTemplate restTemplate;
  private final String baseUrl;

  public NcmService(
      RestTemplate restTemplate,
      @Value("${ncm.base-url:http://localhost:3000}") String baseUrl
  ) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl.replaceAll("/+$", "");
  }

  public Map<String, Object> loginByPassword(HttpSession session, String phone, String password, String countrycode) {
    String cc = (countrycode == null || countrycode.isBlank()) ? "86" : countrycode.trim();
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("phone", phone);
    form.add("password", password);
    form.add("countrycode", cc);
    ResponseEntity<Map> resp = callNcmForm(session, "/login/cellphone", form, false);
    saveCookieFromResponse(session, resp);
    saveCookieFromJsonBody(session, resp.getBody());
    return normalizeLoginResponse(resp.getBody(), session);
  }

  public Map<String, Object> sendCaptcha(HttpSession session, String phone) {
    ResponseEntity<Map> resp = callNcm(session, "/captcha/sent?phone=" + encode(phone), HttpMethod.GET, null, false);
    return safeBody(resp.getBody());
  }

  public Map<String, Object> loginByCaptcha(HttpSession session, String phone, String captcha, String countrycode) {
    String cc = (countrycode == null || countrycode.isBlank()) ? "86" : countrycode.trim();
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("phone", phone);
    form.add("captcha", captcha);
    form.add("countrycode", cc);
    ResponseEntity<Map> resp = callNcmForm(session, "/login/cellphone", form, false);
    saveCookieFromResponse(session, resp);
    saveCookieFromJsonBody(session, resp.getBody());
    return normalizeLoginResponse(resp.getBody(), session);
  }

  public Map<String, Object> loginByCookie(HttpSession session, String rawCookie) {
    if (rawCookie == null || rawCookie.isBlank()) {
      throw new BusinessException(400, "Cookie 不能为空");
    }
    session.setAttribute(SESSION_NCM_COOKIE, rawCookie.trim());
    ResponseEntity<Map> resp = callNcm(session, "/login/status", HttpMethod.GET, null, true);
    saveCookieFromJsonBody(session, resp.getBody());
    return normalizeLoginResponse(resp.getBody(), session);
  }

  public Map<String, Object> loginStatus(HttpSession session) {
    ResponseEntity<Map> status = callNcm(session, "/login/status", HttpMethod.GET, null, true);
    return safeBody(status.getBody());
  }

  /** 对应 @neteasecloudmusicapienhanced/api {@code GET /login/qr/key}，需带时间戳防缓存 */
  public Map<String, Object> qrLoginKey(HttpSession session) {
    String path = "/login/qr/key?t=" + System.currentTimeMillis();
    ResponseEntity<Map> resp = callNcm(session, path, HttpMethod.GET, null, false);
    saveCookieFromResponse(session, resp);
    saveCookieFromJsonBody(session, resp.getBody());
    return safeBody(resp.getBody());
  }

  /** 对应 {@code GET /login/qr/create?key=&qrimg=} */
  public Map<String, Object> qrLoginCreate(HttpSession session, String key, boolean qrimg) {
    String path = "/login/qr/create?key=" + encode(key) + "&qrimg=" + qrimg + "&t=" + System.currentTimeMillis();
    ResponseEntity<Map> resp = callNcm(session, path, HttpMethod.GET, null, false);
    saveCookieFromResponse(session, resp);
    saveCookieFromJsonBody(session, resp.getBody());
    return safeBody(resp.getBody());
  }

  /**
   * 轮询 {@code GET /login/qr/check?key=}；返回体 {@code code}：800 过期、801 待扫、802 待确认、803 成功（含 cookie）。
   */
  public Map<String, Object> qrLoginCheck(HttpSession session, String key) {
    String path = "/login/qr/check?key=" + encode(key) + "&t=" + System.currentTimeMillis();
    ResponseEntity<Map> resp = callNcm(session, path, HttpMethod.GET, null, false);
    saveCookieFromResponse(session, resp);
    saveCookieFromJsonBody(session, resp.getBody());
    Map<String, Object> out = safeBody(resp.getBody());
    out.put("sessionCookiePresent", getCookie(session) != null);
    return out;
  }

  public Map<String, Object> testLosslessUrl(HttpSession session, long songId) {
    String path = "/song/url/v1?id=" + songId + "&level=lossless";
    ResponseEntity<Map> songResp = callNcm(session, path, HttpMethod.GET, null, true);
    Map<String, Object> data = safeBody(songResp.getBody());
    data.put("sessionCookiePresent", getCookie(session) != null);
    return data;
  }

  private ResponseEntity<Map> callNcmForm(
      HttpSession session,
      String path,
      MultiValueMap<String, String> form,
      boolean requiresCookie
  ) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.set("User-Agent", "MyWebsite-NCM-Proxy/1.0");
    String cookie = getCookie(session);
    if (cookie != null) {
      headers.set("Cookie", cookie);
    } else if (requiresCookie) {
      throw new BusinessException(401, "未登录网易云，请先完成登录");
    }
    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);
    try {
      return restTemplate.exchange(baseUrl + path, HttpMethod.POST, entity, Map.class);
    } catch (Exception e) {
      throw new BusinessException(502, "NCM API 请求失败: " + e.getMessage());
    }
  }

  private ResponseEntity<Map> callNcm(
      HttpSession session,
      String path,
      HttpMethod method,
      Object body,
      boolean requiresCookie
  ) {
    HttpHeaders headers = new HttpHeaders();
    if (body != null) {
      headers.setContentType(MediaType.APPLICATION_JSON);
    }
    headers.set("User-Agent", "MyWebsite-NCM-Proxy/1.0");
    String cookie = getCookie(session);
    if (cookie != null) {
      headers.set("Cookie", cookie);
    } else if (requiresCookie) {
      throw new BusinessException(401, "未登录网易云，请先完成登录");
    }
    HttpEntity<?> entity = new HttpEntity<>(body, headers);
    try {
      return restTemplate.exchange(baseUrl + path, method, entity, Map.class);
    } catch (Exception e) {
      throw new BusinessException(502, "NCM API 请求失败: " + e.getMessage());
    }
  }

  private void saveCookieFromJsonBody(HttpSession session, Map<?, ?> body) {
    if (body == null) {
      return;
    }
    Object c = body.get("cookie");
    if (c instanceof String s && !s.isBlank()) {
      mergeSessionCookieFromRaw(session, s.trim());
    }
  }

  /** 合并写入：扫码轮询可能多次返回不同片段，803 时再补全登录 cookie */
  private void mergeSessionCookieFromRaw(HttpSession session, String rawWithMaybeAttrs) {
    Map<String, String> merged = new LinkedHashMap<>();
    putCookiePairsSkipAttrs(merged, getCookie(session));
    putCookiePairsSkipAttrs(merged, rawWithMaybeAttrs);
    String header = merged.entrySet().stream()
        .map(e -> e.getKey() + "=" + e.getValue())
        .collect(Collectors.joining("; "));
    if (!header.isBlank()) {
      session.setAttribute(SESSION_NCM_COOKIE, header);
    }
  }

  /** 解析 cookie 串（可含 Max-Age/Path 等属性段），只把 name=value 放入 map（后者覆盖同名） */
  private void putCookiePairsSkipAttrs(Map<String, String> into, String raw) {
    if (raw == null || raw.isBlank()) {
      return;
    }
    for (String segment : raw.split(";")) {
      String s = segment.trim();
      int eq = s.indexOf('=');
      if (eq <= 0) {
        continue;
      }
      String name = s.substring(0, eq).trim();
      if (COOKIE_ATTR_NAMES.contains(name.toLowerCase(Locale.ROOT))) {
        continue;
      }
      String value = s.substring(eq + 1).trim();
      into.put(name, value);
    }
  }

  private void saveCookieFromResponse(HttpSession session, ResponseEntity<?> resp) {
    List<String> setCookies = resp.getHeaders().get(HttpHeaders.SET_COOKIE);
    if (setCookies == null || setCookies.isEmpty()) return;
    String merged = mergeSetCookie(setCookies);
    if (!merged.isBlank()) {
      mergeSessionCookieFromRaw(session, merged);
    }
  }

  private String mergeSetCookie(List<String> setCookies) {
    Map<String, String> cookieMap = new LinkedHashMap<>();
    for (String raw : setCookies) {
      String first = raw.split(";", 2)[0];
      int idx = first.indexOf('=');
      if (idx <= 0) continue;
      String key = first.substring(0, idx).trim();
      String val = first.substring(idx + 1).trim();
      if (!key.isEmpty()) cookieMap.put(key, val);
    }
    return cookieMap.entrySet().stream()
        .map(e -> e.getKey() + "=" + e.getValue())
        .collect(Collectors.joining("; "));
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> normalizeLoginResponse(Map body, HttpSession session) {
    Map<String, Object> out = safeBody(body);
    out.put("sessionCookiePresent", getCookie(session) != null);
    return out;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> safeBody(Map body) {
    if (body == null) return new HashMap<>();
    return new HashMap<>(body);
  }

  private String getCookie(HttpSession session) {
    Object raw = session.getAttribute(SESSION_NCM_COOKIE);
    if (raw == null) return null;
    String value = String.valueOf(raw).trim();
    return value.isEmpty() ? null : value;
  }

  private String encode(String value) {
    return URLEncoder.encode(Objects.toString(value, ""), StandardCharsets.UTF_8);
  }
}
