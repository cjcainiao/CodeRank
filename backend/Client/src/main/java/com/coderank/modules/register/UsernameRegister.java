package com.coderank.modules.register;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coderank.entity.dto.RegisterDTO;
import com.coderank.entity.pojo.User;
import com.coderank.entity.vo.RegisterVO;
import com.coderank.enums.ResultCode;
import com.coderank.exception.BusinessException;
import com.coderank.mapper.UserMapper;
import com.coderank.utils.PasswordEncryptUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 用户名注册逻辑
 */
@Component
@RequiredArgsConstructor
public class UsernameRegister implements Register {

    private final UserMapper userMapper;

    public RegisterVO doRegister(RegisterDTO registerDTO) {
        // 校验参数
        if (!StringUtils.hasText(registerDTO.getUserName())
                || !StringUtils.hasText(registerDTO.getPassword())
                || !StringUtils.hasText(registerDTO.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PARAMETER_ERROR.getCode(), ResultCode.PARAMETER_ERROR.getMsg());
        }
        // 判断两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException(ResultCode.TWOPASSWOED_ERROR.getCode(), ResultCode.TWOPASSWOED_ERROR.getMsg());
        }
        // 判断用户名是否被使用
        boolean exists = userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getUserName, registerDTO.getUserName()));
        if (exists) {
            throw new BusinessException(ResultCode.USERNAME_ISUSE.getCode(), ResultCode.USERNAME_ISUSE.getMsg());
        }
        // 保存用户信息
        User user = BeanUtil.copyProperties(registerDTO, User.class);
        user.setPassword(PasswordEncryptUtils.encrypt(registerDTO.getPassword()));
        int inserted = userMapper.insert(user);
        if (inserted <= 0) {
            throw new BusinessException("未知错误");
        }

        //返回
        return new RegisterVO();
    }
}
