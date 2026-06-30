package com.weihanyinian.website.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory rate limiter.
 * For production with Redis, replace with Redis-backed implementation.
 */
@Component
public class RateLimiter {

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000; // 1 minute

    public boolean isAllowed(String key) {
        cleanExpired();
        Entry entry = store.compute(key, (k, v) -> {
            if (v == null || v.windowStart + WINDOW_MS < System.currentTimeMillis()) {
                return new Entry(1);
            }
            v.count++;
            return v;
        });
        return entry.count <= MAX_ATTEMPTS;
    }

    public long remainingAttempts(String key) {
        Entry entry = store.get(key);
        if (entry == null || entry.windowStart + WINDOW_MS < System.currentTimeMillis()) {
            return MAX_ATTEMPTS;
        }
        return Math.max(0, MAX_ATTEMPTS - entry.count);
    }

    public long resetAfterSeconds(String key) {
        Entry entry = store.get(key);
        if (entry == null) return 0;
        long elapsed = System.currentTimeMillis() - entry.windowStart;
        long remaining = WINDOW_MS - elapsed;
        return Math.max(0, remaining / 1000);
    }

    private void cleanExpired() {
        long now = System.currentTimeMillis();
        store.entrySet().removeIf(e -> e.getValue().windowStart + WINDOW_MS < now);
    }

    private static class Entry {
        final long windowStart;
        int count;

        Entry(int count) {
            this.windowStart = System.currentTimeMillis();
            this.count = count;
        }
    }
}
