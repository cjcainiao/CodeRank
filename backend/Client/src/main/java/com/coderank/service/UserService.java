package com.coderank.service;

import com.coderank.entity.dto.LoginDTO;
import com.coderank.entity.dto.RegisterDTO;
import com.coderank.entity.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coderank.entity.vo.LoginVO;
import com.coderank.entity.vo.RegisterVO;
import com.coderank.entity.vo.UserInfoVO;
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
    ResponseResult<UserInfoVO> login(LoginDTO loginDTO);

    /**
     * 统一注册接口
     * @param registerDTO
     * @return
     */
    ResponseResult<RegisterVO> register(RegisterDTO registerDTO);

    /**
     * 获取用户信息
     * @return
     */
    ResponseResult<UserInfoVO> getUserInfo();

    /**
     * 用户注销
     * @return
     */
    ResponseResult<Boolean> logout();

    /**
     * 刷新token
     * @param refreshToken
     * @return
     */
    ResponseResult<Boolean> flushToken(String refreshToken);
}
