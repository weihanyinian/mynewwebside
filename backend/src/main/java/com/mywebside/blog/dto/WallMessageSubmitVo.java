package com.mywebside.blog.dto;

/** 访客提交后的回执（内容需审核通过后才会在列表展示）。 */
public record WallMessageSubmitVo(long id, String reviewHint) {}
