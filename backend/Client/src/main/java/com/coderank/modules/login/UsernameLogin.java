package com.coderank.modules.login;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coderank.entity.dto.LoginDTO;
import com.coderank.entity.pojo.User;
import com.coderank.entity.vo.LoginVO;
import com.coderank.enums.ResultCode;
import com.coderank.exception.BusinessException;
import com.coderank.mapper.UserMapper;
import com.coderank.utils.PasswordEncryptUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 用户名登录逻辑
 */
@Component
@RequiredArgsConstructor
public class UsernameLogin implements Login {

    private final UserMapper userMapper;

    public LoginVO doLogin(LoginDTO loginDTO) {
        // 校验参数
        if (Objects.isNull(loginDTO.getUserName()) || Objects.isNull(loginDTO.getPassword())) {
            throw new BusinessException(ResultCode.PARAMETER_ERROR.getCode(), ResultCode.PARAMETER_ERROR.getMsg());
        }
        // 查询用户信息
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserName, loginDTO.getUserName()));
        if (Objects.isNull(user)) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), ResultCode.USER_NOT_LOGIN.getMsg());
        }
        //校验密码
        boolean verify = PasswordEncryptUtils.verify(loginDTO.getPassword(), user.getPassword());
        if (!verify){
            throw new BusinessException(ResultCode.PASSWORD_ERROR.getCode(), ResultCode.PASSWORD_ERROR.getMsg());
        }

        //生成token

        //

        return null;
    }

}
