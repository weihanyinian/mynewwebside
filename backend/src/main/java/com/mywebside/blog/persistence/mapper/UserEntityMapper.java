package com.mywebside.blog.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mywebside.blog.persistence.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserEntityMapper extends BaseMapper<UserEntity> {
}
