package com.weihanyinian.website.module.music.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.net.URI;

@RestController
@RequestMapping("/api/music")
public class MusicController {

    private final RestClient restClient;
    private final String ncmBaseUrl;

    public MusicController(@Value("${ncm.base-url:http://localhost:3000}") String ncmBaseUrl) {
        this.ncmBaseUrl = ncmBaseUrl;
        this.restClient = RestClient.builder().build();
    }

    @RequestMapping("/**")
    public ResponseEntity<?> proxy(HttpServletRequest request, @RequestBody(required = false) String body) {
        String path = request.getRequestURI().replace("/api/music", "");
        String query = request.getQueryString();
        String targetUrl = ncmBaseUrl + path + (query != null ? "?" + query : "");

        try {
            var requestBuilder = restClient.method(HttpMethod.valueOf(request.getMethod()))
                    .uri(URI.create(targetUrl));

            if (body != null && !body.isEmpty()) {
                requestBuilder.body(body);
            }

            String response = requestBuilder
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .body(String.class);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("{\"error\": \"NCM API unavailable: " + e.getMessage() + "\"}");
        }
    }
}
