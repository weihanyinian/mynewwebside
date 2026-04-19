package com.mywebside.blog.service.views;

/** 文章阅读量：默认同步写库；开启 Redis 时仅 INCR 缓存并定时刷库。 */
public interface ArticleViewCounter {

  void recordView(long articleId);

  /** 尚未刷入 MySQL、需在接口展示时叠加的增量。 */
  long pendingDisplayDelta(long articleId);
}
