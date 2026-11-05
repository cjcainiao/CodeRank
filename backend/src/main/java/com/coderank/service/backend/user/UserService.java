package com.coderank.service.backend.user;

import com.coderank.common.result.PageResult;
import com.coderank.entity.dto.user.UserCreateDTO;
import com.coderank.entity.dto.user.UserQueryDTO;
import com.coderank.entity.dto.user.UserUpdateDTO;
import com.coderank.entity.vo.user.UserVO;

/**
 * 后台用户管理业务。
 */
public interface UserService {

    // 分页查询用户
    PageResult<UserVO> pageUsers(UserQueryDTO queryDTO);

    // 按 id 查询用户
    UserVO getUserById(Long id);

    // 新增用户
    Long createUser(UserCreateDTO createDTO);

    // 修改用户
    void updateUser(UserUpdateDTO updateDTO);

    // 删除用户
    void deleteUser(Long id);
}
