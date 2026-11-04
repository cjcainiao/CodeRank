package com.coderank.mapper.backend;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coderank.entity.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台用户数据访问。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
