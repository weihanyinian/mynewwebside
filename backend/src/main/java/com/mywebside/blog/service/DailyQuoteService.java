package com.mywebside.blog.service;

import com.mywebside.blog.config.AppProperties;
import com.mywebside.blog.dto.DailyQuoteDto;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DailyQuoteService {
  private static final ZoneId TZ = ZoneId.of("Asia/Shanghai");

  private final AppProperties appProperties;

  public DailyQuoteService(AppProperties appProperties) {
    this.appProperties = appProperties;
  }

  /** 按上海时区「日期」在配置列表中轮转取一句。 */
  public DailyQuoteDto today() {
    List<String> list = appProperties.getDailyQuotes();
    if (list == null || list.isEmpty()) {
      return new DailyQuoteDto("后端尚未配置 app.daily-quotes，请编辑 application.yml。");
    }
    long day = LocalDate.now(TZ).toEpochDay();
    int n = list.size();
    int i = (int) ((day % n + n) % n);
    return new DailyQuoteDto(list.get(i));
  }
}
