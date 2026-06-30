package com.weihanyinian.website.module.music.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class MusicService {

    private final NcmApiClient ncmApiClient;

    public MusicService(NcmApiClient ncmApiClient) {
        this.ncmApiClient = ncmApiClient;
    }

    public JsonNode search(String keywords, int limit) {
        return ncmApiClient.search(keywords, limit);
    }

    public JsonNode getSongDetail(String ids) {
        return ncmApiClient.getSongDetail(ids);
    }

    public JsonNode getSongUrl(long id) {
        return ncmApiClient.getSongUrl(id);
    }

    public JsonNode getPlaylistDetail(long id) {
        return ncmApiClient.getPlaylistDetail(id);
    }

    public JsonNode getLyric(long id) {
        return ncmApiClient.getLyric(id);
    }

    public JsonNode getTopPlaylists(int limit) {
        return ncmApiClient.getTopPlaylists(limit);
    }

    public JsonNode getRecommendSongs() {
        return ncmApiClient.getRecommendSongs();
    }
}
