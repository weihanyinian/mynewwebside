package com.mywebside.blog.config;

import com.mywebside.blog.common.IpRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {

  /** 注册接口：同一 IP 每 60 秒最多 5 次。 */
  @Bean
  public IpRateLimiter registerRateLimiter() {
    return new IpRateLimiter(5, 60);
  }
}
