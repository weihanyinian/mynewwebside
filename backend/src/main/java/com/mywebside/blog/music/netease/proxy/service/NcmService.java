package com.mywebside.blog.music.netease.proxy.service;

import com.mywebside.blog.common.BusinessException;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NcmService {

  private static final String SESSION_NCM_COOKIE = "ncm_cookie";
  private final RestTemplate restTemplate;
  private final String baseUrl;

  public NcmService(
      RestTemplate restTemplate,
      @Value("${ncm.base-url:http://localhost:3000}") String baseUrl
  ) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl.replaceAll("/+$", "");
  }

  public Map<String, Object> loginByPassword(HttpSession session, String phone, String password) {
    String query = "phone=" + encode(phone) + "&password=" + encode(password);
    ResponseEntity<Map> resp = callNcm(session, "/login/cellphone?" + query, HttpMethod.POST, null, false);
    saveCookieFromResponse(session, resp);
    return normalizeLoginResponse(resp.getBody(), session);
  }

  public Map<String, Object> sendCaptcha(HttpSession session, String phone) {
    ResponseEntity<Map> resp = callNcm(session, "/captcha/sent?phone=" + encode(phone), HttpMethod.GET, null, false);
    return safeBody(resp.getBody());
  }

  public Map<String, Object> loginByCaptcha(HttpSession session, String phone, String captcha) {
    String query = "phone=" + encode(phone) + "&captcha=" + encode(captcha);
    ResponseEntity<Map> resp = callNcm(session, "/login/cellphone?" + query, HttpMethod.POST, null, false);
    saveCookieFromResponse(session, resp);
    return normalizeLoginResponse(resp.getBody(), session);
  }

  public Map<String, Object> loginByCookie(HttpSession session, String rawCookie) {
    if (rawCookie == null || rawCookie.isBlank()) {
      throw new BusinessException(400, "Cookie 不能为空");
    }
    session.setAttribute(SESSION_NCM_COOKIE, rawCookie.trim());
    ResponseEntity<Map> resp = callNcm(session, "/login/status", HttpMethod.GET, null, true);
    return normalizeLoginResponse(resp.getBody(), session);
  }

  public Map<String, Object> loginStatus(HttpSession session) {
    ResponseEntity<Map> status = callNcm(session, "/login/status", HttpMethod.GET, null, true);
    return safeBody(status.getBody());
  }

  public Map<String, Object> testLosslessUrl(HttpSession session, long songId) {
    String path = "/song/url/v1?id=" + songId + "&level=lossless";
    ResponseEntity<Map> songResp = callNcm(session, path, HttpMethod.GET, null, true);
    Map<String, Object> data = safeBody(songResp.getBody());
    data.put("sessionCookiePresent", getCookie(session) != null);
    return data;
  }

  private ResponseEntity<Map> callNcm(
      HttpSession session,
      String path,
      HttpMethod method,
      Object body,
      boolean requiresCookie
  ) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("User-Agent", "MyWebSide-NCM-Proxy/1.0");
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

  private void saveCookieFromResponse(HttpSession session, ResponseEntity<?> resp) {
    List<String> setCookies = resp.getHeaders().get(HttpHeaders.SET_COOKIE);
    if (setCookies == null || setCookies.isEmpty()) return;
    String merged = mergeSetCookie(setCookies);
    if (!merged.isBlank()) {
      session.setAttribute(SESSION_NCM_COOKIE, merged);
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
