package com.coderank.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.coderank.constants.HeaderConstants;
import com.coderank.context.LoginUser;
import com.coderank.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 获取用户信息拦截器
 */
@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class UserInfoInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    public UserInfoInterceptor() {
        this.objectMapper = new ObjectMapper();
    }

    public UserInfoInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userInfo = request.getHeader(HeaderConstants.X_USER_INFO);

        LoginUser user = null;
        if (userInfo != null && !userInfo.isBlank()) {
            try {
                user = objectMapper.readValue(userInfo, LoginUser.class);
            } catch (Exception e) {
                // 解析失败按未登录处理，由业务层决定是否放行/拒绝
                user = null;
            }
        }

        UserContext.set(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 关键：请求结束必须清理，避免线程池复用导致串号
        UserContext.clear();
    }
}
