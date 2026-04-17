package com.mywebside.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mywebside.blog.admin.dto.UserListItemDto;
import com.mywebside.blog.common.ApiResponse;
import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.persistence.entity.UserEntity;
import com.mywebside.blog.persistence.mapper.UserEntityMapper;
import com.mywebside.blog.user.UserRoles;
import java.time.ZoneId;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

  private final UserEntityMapper userMapper;

  public AdminUserController(UserEntityMapper userMapper) {
    this.userMapper = userMapper;
  }

  @GetMapping
  public ApiResponse<List<UserListItemDto>> list() {
    List<UserEntity> all = userMapper.selectList(new LambdaQueryWrapper<UserEntity>().orderByAsc(UserEntity::getId));
    return ApiResponse.ok(all.stream().map(this::toDto).toList());
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable long id) {
    UserEntity u = userMapper.selectById(id);
    if (u == null) {
      throw new BusinessException(404, "用户不存在");
    }
    if (UserRoles.isBuiltInAdmin(u.getUsername())) {
      throw new BusinessException(400, "不能删除内置管理员账号");
    }
    userMapper.deleteById(id);
    return ApiResponse.ok();
  }

  private UserListItemDto toDto(UserEntity u) {
    String at = u.getCreatedAt() == null
        ? ""
        : u.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toString();
    return new UserListItemDto(u.getId(), u.getUsername(), u.getNickname(), at);
  }
}
