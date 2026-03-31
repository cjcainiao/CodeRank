package com.coderank.interceptor;

import com.coderank.constants.USER_PREFIX;
import com.coderank.enums.ResultCode;
import com.coderank.exception.BusinessException;
import com.coderank.utils.JwtUtils;
import com.coderank.utils.UserContextUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * 认证拦截器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final RedisTemplate redisTemplate;

    /**
     * 认证拦截
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取token
        String token = request.getHeader("Authorization");
        if (Objects.isNull(token) || !StringUtils.hasText(token)) {
            throw new BusinessException(ResultCode.NO_PERMISSION.getCode(), ResultCode.NO_PERMISSION.getMsg());
        }

        //解析token(存放到用户上下文信息)
        Claims claims = jwtUtils.parseToken(token);
        String userId = claims.get("userId", String.class);
        UserContextUtils.setUser(Long.parseLong(userId));

        //-------单点登录逻辑----------

        SinglePoint(userId, token);

        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContextUtils.clear();
    }

    /**
     * 单点登录逻辑
     *
     * @param userId
     */
    private void SinglePoint(String userId, String token) {
        // 从缓存中拿到token信息
        String cacheToken = (String) redisTemplate.opsForValue().get(USER_PREFIX.USER_TOKEN + userId);
        if (Objects.isNull(cacheToken)) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN.getCode(), ResultCode.USER_NOT_LOGIN.getMsg());
        }

        //判断token是否失效
        if (!cacheToken.equals(token)) {
            throw new BusinessException(ResultCode.LOGIN_IS_EXPIRED.getCode(), ResultCode.LOGIN_IS_EXPIRED.getMsg());
        }
    }
}
