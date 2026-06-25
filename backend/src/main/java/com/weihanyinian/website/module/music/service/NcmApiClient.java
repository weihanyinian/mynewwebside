package com.weihanyinian.website.module.music.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class NcmApiClient {

    private final RestClient restClient;
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public NcmApiClient(@Value("${ncm.base-url:http://localhost:3000}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restClient = RestClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Search for songs
     */
    public JsonNode search(String keywords, int limit) {
        String url = baseUrl + "/search?keywords=" + urlEncode(keywords) + "&limit=" + limit;
        return get(url);
    }

    /**
     * Get song detail by id(s)
     */
    public JsonNode getSongDetail(String ids) {
        return get(baseUrl + "/song/detail?ids=" + ids);
    }

    /**
     * Get playable song URL
     */
    public JsonNode getSongUrl(long id) {
        return get(baseUrl + "/song/url?id=" + id);
    }

    /**
     * Get playlist detail
     */
    public JsonNode getPlaylistDetail(long id) {
        return get(baseUrl + "/playlist/detail?id=" + id);
    }

    /**
     * Get lyric by song id
     */
    public JsonNode getLyric(long id) {
        return get(baseUrl + "/lyric?id=" + id);
    }

    /**
     * Get top playlists
     */
    public JsonNode getTopPlaylists(int limit) {
        return get(baseUrl + "/top/playlist?limit=" + limit);
    }

    /**
     * Get daily recommended songs (requires login cookie)
     */
    public JsonNode getRecommendSongs() {
        return get(baseUrl + "/recommend/songs");
    }

    private JsonNode get(String url) {
        try {
            String body = restClient.get()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .body(String.class);
            if (body == null) return null;
            return objectMapper.readTree(body);
        } catch (Exception e) {
            return null;
        }
    }

    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }
}
