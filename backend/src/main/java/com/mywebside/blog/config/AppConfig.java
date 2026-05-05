package com.mywebsite.blog.config;

import com.mywebsite.blog.ai.AiCompanionProperties;
import com.mywebsite.blog.music.netease.config.NeteaseMusicOpenProperties;
import com.mywebsite.blog.music.netease.proxy.config.NeteaseProxyProperties;
import com.mywebsite.blog.music.qq.config.QqMusicProxyProperties;
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
