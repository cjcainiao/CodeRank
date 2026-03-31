package com.coderank.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coderank.constants.USER_PREFIX;
import com.coderank.entity.dto.LoginDTO;
import com.coderank.entity.dto.RegisterDTO;
import com.coderank.entity.pojo.User;
import com.coderank.entity.vo.LoginVO;
import com.coderank.entity.vo.RegisterVO;
import com.coderank.entity.vo.UserInfoVO;
import com.coderank.enums.LoginType;
import com.coderank.enums.RegisterType;
import com.coderank.enums.ResultCode;
import com.coderank.exception.BusinessException;
import com.coderank.modules.login.LoginManager;
import com.coderank.modules.register.RegisterManager;
import com.coderank.service.UserService;
import com.coderank.mapper.UserMapper;
import com.coderank.utils.JwtUtils;
import com.coderank.utils.ResponseResult;
import com.coderank.utils.UserContextUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 用户相关业务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final LoginManager loginManager;
    private final RegisterManager registerManager;
    private final RedisTemplate redisTemplate;
    private final JwtUtils jwtUtils;

    /**
     * 通一登录业务
     *
     * @param loginDTO
     * @return
     */
    public ResponseResult<UserInfoVO> login(LoginDTO loginDTO) {
        return ResponseResult.success(
                loginManager.getLogin(
                                LoginType.getByType(loginDTO.getType()))
                        .doLogin(loginDTO));
    }

    /**
     * 统一注册业务
     *
     * @param registerDTO
     * @return
     */
    public ResponseResult<RegisterVO> register(RegisterDTO registerDTO) {
        return ResponseResult.success(
                registerManager.getRegister(
                                RegisterType.getByType(registerDTO.getType()))
                        .doRegister(registerDTO));

    }

    /**
     * 获取用户信息
     *
     * @return
     */
    public ResponseResult<UserInfoVO> getUserInfo() {
        //从用户上下文获取用户id
        Long userId = UserContextUtils.getUser();
        //从redis中获取用户登录信息
        UserInfoVO userInfovo = (UserInfoVO) redisTemplate.opsForValue().get(USER_PREFIX.LOGIN_USER + userId);
        if (Objects.isNull(userInfovo)) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN.getCode(), ResultCode.USER_NOT_LOGIN.getMsg());
        }
        return ResponseResult.success(userInfovo);
    }

    /**
     * 用户注销
     *
     * @return
     */
    public ResponseResult<Boolean> logout() {
        // 从用户上下文获取用户id
        Long userId = UserContextUtils.getUser();
        //清空缓存
        flush_userInfo(userId.toString());
        return ResponseResult.success(true);
    }

    /**
     * 刷新token
     *
     * @param refreshToken
     * @return
     */
    @Override
    public ResponseResult<Boolean> flushToken(String refreshToken) {
        // 解析token
        Claims claims = jwtUtils.parseToken(refreshToken);

        String s = claims.get("userId", String.class);
        Long userId = Long.parseLong(s);

        //查询当前用户信息
        User user = this.getById(userId);
        if (Objects.isNull(user)) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), ResultCode.USER_NOT_FOUND.getMsg());
        }

        //重建token
        Map<String, String> info = new HashMap<>();
        info.put("userId", user.getUserId().toString());
        info.put("username", user.getUserName());
        String token = jwtUtils.createToken(info);

        //重建缓存
        UserInfoVO userInfoVO = BeanUtil.copyProperties(user, UserInfoVO.class);
        cache_userInfo(userInfoVO, token);

        //返回信息
        return ResponseResult.success(true);
    }

    /**
     * 清除用户登录信息
     *
     * @param userId
     */
    private void flush_userInfo(String userId) {
        redisTemplate.delete(USER_PREFIX.LOGIN_USER + userId);
        redisTemplate.delete(USER_PREFIX.USER_TOKEN + userId);
    }

    /**
     * 缓存用户登录信息
     *
     * @param user
     * @param token
     */
    private void cache_userInfo(UserInfoVO user, String token) {
        //缓存用户登录信息 (缓存时间跟确认token一致)
        redisTemplate.opsForValue().set(USER_PREFIX.LOGIN_USER + user.getUserId(), user, jwtUtils.getExpire(), TimeUnit.SECONDS);
        //缓存token信息
        redisTemplate.opsForValue().set(USER_PREFIX.USER_TOKEN + user.getUserId(), token, jwtUtils.getExpire(), TimeUnit.SECONDS);
    }
}




