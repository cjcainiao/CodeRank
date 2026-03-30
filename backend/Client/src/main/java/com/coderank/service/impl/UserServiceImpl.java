package com.coderank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coderank.entity.dto.LoginDTO;
import com.coderank.entity.pojo.User;
import com.coderank.entity.vo.LoginVO;
import com.coderank.modules.login.LoginManager;
import com.coderank.service.UserService;
import com.coderank.mapper.UserMapper;
import com.coderank.utils.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户相关业务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final LoginManager loginManager;

    /**
     * 通一登录业务
     * @param loginDTO
     * @return
     */
    public ResponseResult<LoginVO> login(LoginDTO loginDTO) {
        return ResponseResult.success(
                loginManager.getLogin(
                                loginDTO.getType())
                        .doLogin(loginDTO));
    }
}




