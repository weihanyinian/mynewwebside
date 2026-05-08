package com.mywebside.blog.oj;

import com.mywebside.blog.oj.domain.JudgeResult;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 判题任务状态存储（内存）。
 * <p>适用于单实例部署；任务结果保留 5 分钟后自动清理。</p>
 */
@Component
public class JudgeTaskStore {

  private static final long EXPIRE_MS = 5 * 60 * 1000;

  record Task(String id, JudgeResult result, long createdAt) {
    boolean isDone() {
      return result != null;
    }
  }

  private final ConcurrentMap<String, Task> tasks = new ConcurrentHashMap<>();

  public void putPending(String id) {
    tasks.put(id, new Task(id, null, System.currentTimeMillis()));
  }

  public void putResult(String id, JudgeResult result) {
    Task old = tasks.get(id);
    if (old != null) {
      tasks.put(id, new Task(id, result, old.createdAt));
    }
  }

  public Task get(String id) {
    return tasks.get(id);
  }

  /** 清理过期任务 — 每 5 分钟执行一次。 */
  @Scheduled(fixedRate = 300_000)
  public void cleanup() {
    long now = System.currentTimeMillis();
    tasks.entrySet().removeIf(e -> (now - e.getValue().createdAt) > EXPIRE_MS);
  }
}
