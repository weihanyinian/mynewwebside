package com.mywebside.blog.common;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 轻量级 IP 速率限制器（基于滑动窗口，无需 Redis）。
 * <p>适用于单实例部署的个人博客，防止接口被暴力刷请求。</p>
 * <p>每 5 分钟自动清理过期 bucket，防止内存无限增长。</p>
 */
public class IpRateLimiter {

  private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();
  private final int maxRequests;
  private final long windowSeconds;

  public IpRateLimiter(int maxRequests, long windowSeconds) {
    this.maxRequests = maxRequests;
    this.windowSeconds = windowSeconds;
  }

  /**
   * 尝试获取许可，返回 true 表示允许通过。
   */
  public boolean tryAcquire(String ip) {
    long now = Instant.now().getEpochSecond();
    Bucket bucket = buckets.computeIfAbsent(ip, k -> new Bucket());
    synchronized (bucket) {
      long windowStart = now - windowSeconds;
      if (bucket.windowStart < windowStart) {
        // 窗口过期，重置
        bucket.count.set(1);
        bucket.windowStart = now;
        return true;
      }
      return bucket.count.incrementAndGet() <= maxRequests;
    }
  }

  /** 清理过期的 bucket — 每 5 分钟执行一次，防止内存泄漏。 */
  @Scheduled(fixedRate = 300_000)
  public void cleanup() {
    long now = Instant.now().getEpochSecond();
    long threshold = now - windowSeconds * 2;
    buckets.entrySet().removeIf(e -> {
      Bucket b = e.getValue();
      synchronized (b) {
        return b.windowStart < threshold;
      }
    });
  }

  private static class Bucket {
    final AtomicInteger count = new AtomicInteger(0);
    volatile long windowStart;
  }
}

