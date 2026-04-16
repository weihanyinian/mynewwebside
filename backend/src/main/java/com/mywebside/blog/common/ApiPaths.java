package com.mywebside.blog.common;

/**
 * RESTful 路径约定（仅文档常量，避免魔法字符串散落）。
 * <ul>
 *   <li>{@code /api/public/**} 访客可读/可写（无 JWT）</li>
 *   <li>{@code /api/admin/**} 管理端（需 Bearer JWT + ROLE_ADMIN）</li>
 *   <li>{@code /api/auth/**} 认证</li>
 *   <li>{@code /api/oj/**} 在线判题</li>
 * </ul>
 */
public final class ApiPaths {

  public static final String PREFIX_PUBLIC = "/api/public";
  public static final String PREFIX_ADMIN = "/api/admin";
  public static final String PREFIX_AUTH = "/api/auth";
  public static final String PREFIX_OJ = "/api/oj";

  private ApiPaths() {}
}
