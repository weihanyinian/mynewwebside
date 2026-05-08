package com.mywebside.blog.service.views;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.view-counter", name = "redis", havingValue = "true")
public class RedisArticleViewCounter implements ArticleViewCounter {

  static final String KEY_PREFIX = "blog:vp:";
  static final String DIRTY_SET = "blog:vp:dirty";

  private final StringRedisTemplate redis;

  public RedisArticleViewCounter(StringRedisTemplate redis) {
    this.redis = redis;
  }

  @Override
  public void recordView(long articleId) {
    String k = KEY_PREFIX + articleId;
    redis.opsForValue().increment(k);
    redis.opsForSet().add(DIRTY_SET, String.valueOf(articleId));
  }

  @Override
  public long pendingDisplayDelta(long articleId) {
    String v = redis.opsForValue().get(KEY_PREFIX + articleId);
    if (v == null) {
      return 0;
    }
    try {
      return Long.parseLong(v);
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}
