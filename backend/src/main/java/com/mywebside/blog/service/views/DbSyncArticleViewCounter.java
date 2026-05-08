package com.mywebside.blog.service.views;

import com.mywebside.blog.repo.ArticleRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.view-counter", name = "redis", havingValue = "false", matchIfMissing = true)
public class DbSyncArticleViewCounter implements ArticleViewCounter {

  private final ArticleRepository articleRepo;

  public DbSyncArticleViewCounter(ArticleRepository articleRepo) {
    this.articleRepo = articleRepo;
  }

  @Override
  public void recordView(long articleId) {
    articleRepo.incViews(articleId);
  }

  @Override
  public long pendingDisplayDelta(long articleId) {
    return 0;
  }
}
