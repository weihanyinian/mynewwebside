package com.mywebside.blog.service.views;

import com.mywebside.blog.repo.ArticleRepository;
import java.util.Collections;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 将 Redis 中待刷新的阅读量增量合并进 MySQL。Lua 保证读取增量与扣减 key 原子性。
 */
@Service
@ConditionalOnProperty(prefix = "app.view-counter", name = "redis", havingValue = "true")
public class ArticleViewFlushService {

  private static final DefaultRedisScript<Long> TAKE_AND_SUBTRACT = new DefaultRedisScript<>();

  static {
    TAKE_AND_SUBTRACT.setScriptText(
        """
            local v = redis.call('GET', KEYS[1])
            if v == false then return 0 end
            local n = tonumber(v)
            if n == nil or n <= 0 then return 0 end
            redis.call('DECRBY', KEYS[1], n)
            return n
            """);
    TAKE_AND_SUBTRACT.setResultType(Long.class);
  }

  private final StringRedisTemplate redis;
  private final ArticleRepository articleRepo;

  public ArticleViewFlushService(StringRedisTemplate redis, ArticleRepository articleRepo) {
    this.redis = redis;
    this.articleRepo = articleRepo;
  }

  @Scheduled(fixedDelayString = "${app.view-counter.flush-ms:60000}")
  @Transactional
  public void flushPendingViews() {
    Set<String> dirty = redis.opsForSet().members(RedisArticleViewCounter.DIRTY_SET);
    if (dirty == null || dirty.isEmpty()) {
      return;
    }
    for (String idStr : dirty) {
      String key = RedisArticleViewCounter.KEY_PREFIX + idStr;
      Long delta = redis.execute(TAKE_AND_SUBTRACT, Collections.singletonList(key));
      if (delta != null && delta > 0) {
        articleRepo.incrementViewsBy(Long.parseLong(idStr), delta);
      }
      redis.opsForSet().remove(RedisArticleViewCounter.DIRTY_SET, idStr);
    }
  }
}
