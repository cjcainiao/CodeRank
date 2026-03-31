package com.coderank.modules.login;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coderank.constants.USER_PREFIX;
import com.coderank.entity.dto.LoginDTO;
import com.coderank.entity.pojo.User;
import com.coderank.entity.vo.LoginVO;
import com.coderank.entity.vo.UserInfoVO;
import com.coderank.enums.ResultCode;
import com.coderank.exception.BusinessException;
import com.coderank.mapper.UserMapper;
import com.coderank.utils.HttpResponseUtils;
import com.coderank.utils.JwtUtils;
import com.coderank.utils.PasswordEncryptUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 用户名登录逻辑
 */
@Component
@RequiredArgsConstructor
public class UsernameLogin implements Login {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final RedisTemplate redisTemplate;

    public UserInfoVO doLogin(LoginDTO loginDTO) {
        // 校验参数
        if (!StringUtils.hasText(loginDTO.getUserName()) || !StringUtils.hasText(loginDTO.getPassword())) {
            throw new BusinessException(ResultCode.PARAMETER_ERROR.getCode(), ResultCode.PARAMETER_ERROR.getMsg());
        }
        // 查询用户信息
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserName, loginDTO.getUserName()));
        if (Objects.isNull(user)) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), ResultCode.USER_NOT_FOUND.getMsg());
        }
        //校验密码是否正确
        checkPassword(loginDTO.getPassword(), user.getPassword());
        //判断用户账号是否锁定
        user_is_locked(user);
        //生成token
        String token = create_token(user, loginDTO.getRememberMe());
        //缓存用户信息
        UserInfoVO userInfoVO = cache_userInfo(user, token);

        return userInfoVO;
    }

    /**
     * 缓存用户信息
     *
     * @param user
     * @return
     */
    private UserInfoVO cache_userInfo(User user, String token) {
        UserInfoVO userInfoVO = BeanUtil.copyProperties(user, UserInfoVO.class);
        // todo 查询用户权限信息列表

        redisTemplate.opsForValue().set(USER_PREFIX.LOGIN_USER + user.getUserId(), userInfoVO, jwtUtils.getExpire(), TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(USER_PREFIX.USER_TOKEN + user.getUserId(), token, jwtUtils.getExpire(), TimeUnit.SECONDS);

        return userInfoVO;
    }


    /**
     * 生成token
     *
     * @param user
     * @param rememberme
     * @return
     */
    private String create_token(User user, Boolean rememberme) {
        Map<String, String> claims = new HashMap<>();
        claims.put("userId", user.getUserId().toString());
        claims.put("username", user.getUserName());
        //生成确认token
        String accessToken = jwtUtils.createToken(claims);
        HttpResponseUtils.setToken("accessToken", accessToken);

        //生成刷新token
        if (rememberme) {
            String refreshToken = jwtUtils.flush_token(claims);
            HttpResponseUtils.setToken("refreshToken", refreshToken);
        }
        return accessToken;
    }

    /**
     * 判断账号是否锁定
     *
     * @param user
     */
    private void user_is_locked(User user) {
        if (user.getStatus().equals("2")) {
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED.getCode(), ResultCode.ACCOUNT_LOCKED.getMsg());
        }
    }

    /**
     * 校验密码是否正确
     *
     * @param rawPassword
     * @param password
     * @return
     */
    private Boolean checkPassword(String rawPassword, String password) {
        boolean b = PasswordEncryptUtils.verify(rawPassword, password);
        if (!b) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR.getCode(), ResultCode.PASSWORD_ERROR.getMsg());
        }
        return true;
    }

}
