package com.coderank.service;

import com.coderank.entity.dto.LoginDTO;
import com.coderank.entity.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coderank.entity.vo.LoginVO;
import com.coderank.utils.ResponseResult;

/**
 * 用户相关业务接口
 */
public interface UserService extends IService<User> {

    /**
     * 统一登录接口
     * @param loginDTO
     * @return
     */
    ResponseResult<LoginVO> login(LoginDTO loginDTO);
}
