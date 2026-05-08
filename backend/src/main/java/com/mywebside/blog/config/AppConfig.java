package com.mywebside.blog.config;

import com.mywebside.blog.ai.AiCompanionProperties;
import com.mywebside.blog.music.netease.config.NeteaseMusicOpenProperties;
import com.mywebside.blog.music.netease.proxy.config.NeteaseProxyProperties;
import com.mywebside.blog.music.qq.config.QqMusicProxyProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    AppProperties.class,
    AiCompanionProperties.class,
    NeteaseMusicOpenProperties.class,
    NeteaseProxyProperties.class,
    QqMusicProxyProperties.class
})
public class AppConfig {}
