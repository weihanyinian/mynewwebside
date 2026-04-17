package com.mywebside.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mywebside.blog.auth.LoginRequest;
import com.mywebside.blog.auth.LoginResponse;
import com.mywebside.blog.auth.RegisterRequest;
import com.mywebside.blog.auth.UserInfoDto;
import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.persistence.entity.UserEntity;
import com.mywebside.blog.persistence.mapper.UserEntityMapper;
import com.mywebside.blog.security.JwtService;
import com.mywebside.blog.user.UserRoles;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService {

  private final UserEntityMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public UserAccountService(UserEntityMapper userMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  @Transactional
  public void register(RegisterRequest req) {
    String name = req.username().trim();
    if (UserRoles.isBuiltInAdmin(name)) {
      throw new BusinessException(400, "该用户名不可注册");
    }
    if (userMapper.selectCount(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, name)) > 0) {
      throw new BusinessException(400, "用户名已存在");
    }
    UserEntity u = new UserEntity();
    u.setUsername(name);
    u.setPassword(passwordEncoder.encode(req.password()));
    u.setNickname(req.nickname().trim());
    u.setCreatedAt(LocalDateTime.now());
    userMapper.insert(u);
  }

  public LoginResponse login(LoginRequest req) {
    String name = req.username().trim();
    UserEntity u = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, name));
    if (u == null || !passwordEncoder.matches(req.password(), u.getPassword())) {
      throw new BusinessException(401, "账号或密码错误");
    }
    boolean admin = UserRoles.isBuiltInAdmin(u.getUsername());
    String token = jwtService.issueToken(u.getUsername());
    return new LoginResponse(token, u.getUsername(), u.getNickname(), admin);
  }

  public UserEntity requireByUsername(String username) {
    UserEntity u = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, username));
    if (u == null) {
      throw new BusinessException(401, "用户不存在或已删除");
    }
    return u;
  }

  public long requireUserId(String username) {
    return requireByUsername(username).getId();
  }

  public UserInfoDto toInfo(UserEntity u) {
    return new UserInfoDto(u.getId(), u.getUsername(), u.getNickname(), UserRoles.isBuiltInAdmin(u.getUsername()));
  }
}
