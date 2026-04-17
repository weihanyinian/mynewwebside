package com.mywebside.blog.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mywebside.blog.config.AppProperties;
import com.mywebside.blog.persistence.entity.UserEntity;
import com.mywebside.blog.persistence.mapper.UserEntityMapper;
import java.time.LocalDateTime;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 若配置了 {@code app.bootstrap-admin-password}（建议仅通过环境变量注入），且库中尚无 admin 用户，则创建之。
 * 勿在仓库中提交明文生产密码。
 */
@Component
@Order(1)
public class AdminUserBootstrapRunner implements ApplicationRunner {

  private final AppProperties appProperties;
  private final UserEntityMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  public AdminUserBootstrapRunner(AppProperties appProperties, UserEntityMapper userMapper, PasswordEncoder passwordEncoder) {
    this.appProperties = appProperties;
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(ApplicationArguments args) {
    String raw = appProperties.getBootstrapAdminPassword();
    if (!StringUtils.hasText(raw)) {
      return;
    }
    String encoded = passwordEncoder.encode(raw.trim());
    UserEntity existing = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, "admin"));
    if (existing == null) {
      UserEntity u = new UserEntity();
      u.setUsername("admin");
      u.setPassword(encoded);
      u.setNickname("管理员");
      u.setCreatedAt(LocalDateTime.now());
      userMapper.insert(u);
      return;
    }
    if (appProperties.isBootstrapAdminSyncOnStartup()) {
      existing.setPassword(encoded);
      userMapper.updateById(existing);
    }
  }
}
