package com.mywebside.blog.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/** MyBatis-Plus：与 JPA 共用同一数据源；实体见 persistence 包。 */
@Configuration
@MapperScan("com.mywebside.blog.persistence.mapper")
public class MybatisPlusConfig {
}
