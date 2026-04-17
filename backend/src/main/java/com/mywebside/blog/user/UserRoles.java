package com.mywebside.blog.user;

public final class UserRoles {
  private UserRoles() {
  }

  /** 仅用户名为 admin 的账号具备后台权限（与数据库角色字段无关）。 */
  public static boolean isBuiltInAdmin(String username) {
    return username != null && "admin".equalsIgnoreCase(username.trim());
  }
}
