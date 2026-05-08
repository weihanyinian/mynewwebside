package com.mywebside.blog.oj;

import java.util.Arrays;
import java.util.stream.Collectors;

/** 输出规范化：统一换行、逐行 trim，便于 ACM / 力扣样例比对。 */
public final class OjOutputNormalize {

  private OjOutputNormalize() {}

  public static String normalize(String s) {
    if (s == null) {
      return "";
    }
    String t = s.replace("\r\n", "\n").replace('\r', '\n');
    return Arrays.stream(t.split("\n", -1)).map(String::trim).collect(Collectors.joining("\n")).strip();
  }

  public static boolean same(String expected, String actual) {
    return normalize(expected).equals(normalize(actual));
  }
}
