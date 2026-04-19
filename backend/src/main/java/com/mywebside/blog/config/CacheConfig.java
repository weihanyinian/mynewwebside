package com.mywebside.blog.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Caffeine 本地缓存配置。
 * <p>缓存策略：写后 10 分钟过期，最大容量 200 条。</p>
 * <ul>
 *   <li>{@code tags} — 标签列表（低频变更）</li>
 *   <li>{@code categories} — 分类列表（低频变更）</li>
 *   <li>{@code friends} — 友链列表（低频变更）</li>
 * </ul>
 */
@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public CacheManager cacheManager() {
    CaffeineCacheManager cm = new CaffeineCacheManager("tags", "categories", "friends");
    cm.setCaffeine(Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .maximumSize(200));
    return cm;
  }
}
