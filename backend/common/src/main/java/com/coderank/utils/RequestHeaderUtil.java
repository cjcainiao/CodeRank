package com.coderank.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Servlet 请求头读取工具。
 */
public final class RequestHeaderUtil {

    private RequestHeaderUtil() {
    }

    /**
     * 获取当前 Spring MVC 请求中指定请求头的内容。
     *
     * @param name 请求头名称
     * @return 请求头内容；请求头不存在时返回 {@code null}
     */
    public static String getHeader(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("请求头名称不能为空");
        }
        if (name.indexOf('\r') >= 0 || name.indexOf('\n') >= 0) {
            throw new IllegalArgumentException("请求头名称不能包含换行符");
        }

        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            throw new IllegalStateException("当前线程不在 Spring MVC 请求上下文中");
        }

        HttpServletRequest request = servletAttributes.getRequest();
        return request.getHeader(name);
    }
}
