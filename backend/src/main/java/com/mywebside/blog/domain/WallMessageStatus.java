package com.mywebside.blog.domain;

/**
 * 留言墙审核状态：仅 {@link #APPROVED} 对访客可见。
 */
public enum WallMessageStatus {
  /** 待审核 */
  PENDING,
  /** 已通过 */
  APPROVED,
  /** 已拒绝（前台不展示） */
  REJECTED
}
