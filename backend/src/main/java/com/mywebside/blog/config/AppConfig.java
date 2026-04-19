package com.mywebside.blog.config;

import com.mywebside.blog.music.netease.config.NeteaseMusicOpenProperties;
import com.mywebside.blog.music.netease.proxy.config.NeteaseProxyProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AppProperties.class, NeteaseMusicOpenProperties.class, NeteaseProxyProperties.class})
public class AppConfig {}
