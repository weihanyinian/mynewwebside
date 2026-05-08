package com.mywebside.blog.oj;

import com.mywebside.blog.BlogBackendApplication;

/**
 * OJ 模块不单独启动：与 {@link BlogBackendApplication} 同一进程运行。
 * <p>此类仅作包入口说明占位，避免误以为需要第二个 {@code main}。</p>
 */
public final class OJApplication {

  private OJApplication() {}

  /** 与全站后端共用同一启动类 */
  public static Class<?> hostApplication() {
    return BlogBackendApplication.class;
  }
}
