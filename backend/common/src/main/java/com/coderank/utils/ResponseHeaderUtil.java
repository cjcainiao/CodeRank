package com.coderank.utils;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Servlet 响应头写入工具。
 *
 * <p>{@link #setHeader(HttpServletResponse, String, String)} 会覆盖同名响应头，
 * {@link #addHeader(HttpServletResponse, String, String)} 会保留原值并追加新值。</p>
 */
public final class ResponseHeaderUtil {

    private static final String BEARER_PREFIX = "Bearer ";

    private ResponseHeaderUtil() {
    }

    /**
     * 设置当前 Spring MVC 请求的响应头；同名响应头已存在时覆盖原值。
     */
    public static void setHeader(String name, String value) {
        setHeader(currentResponse(), name, value);
    }

    /**
     * 设置响应头；同名响应头已存在时覆盖原值。
     */
    public static void setHeader(HttpServletResponse response, String name, String value) {
        validate(response, name, value);
        response.setHeader(name, value);
    }

    /**
     * 追加响应头；同名响应头已存在时保留原值。
     */
    public static void addHeader(HttpServletResponse response, String name, String value) {
        validate(response, name, value);
        response.addHeader(name, value);
    }

    /**
     * 追加当前 Spring MVC 请求的响应头；同名响应头已存在时保留原值。
     */
    public static void addHeader(String name, String value) {
        addHeader(currentResponse(), name, value);
    }

    /**
     * 将 Bearer Token 写入 Authorization 响应头。
     */
    public static void setBearerToken(HttpServletResponse response, String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("token 不能为空");
        }
        validateHeaderContent(token, "token");
        setHeader(response, HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token.trim());
    }

    /**
     * 将 Bearer Token 写入当前 Spring MVC 请求的 Authorization 响应头。
     */
    public static void setBearerToken(String token) {
        setBearerToken(currentResponse(), token);
    }

    private static HttpServletResponse currentResponse() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            throw new IllegalStateException("当前线程不在 Spring MVC 请求上下文中");
        }

        HttpServletResponse response = servletAttributes.getResponse();
        if (response == null) {
            throw new IllegalStateException("无法获取当前 HTTP 响应");
        }
        return response;
    }

    private static void validate(HttpServletResponse response, String name, String value) {
        if (response == null) {
            throw new IllegalArgumentException("response 不能为空");
        }
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("响应头名称不能为空");
        }
        if (value == null) {
            throw new IllegalArgumentException("响应头内容不能为空");
        }
        validateHeaderContent(name, "响应头名称");
        validateHeaderContent(value, "响应头内容");
    }

    /**
     * 禁止换行符进入响应头，避免 HTTP 响应拆分攻击。
     */
    private static void validateHeaderContent(String content, String fieldName) {
        if (content.indexOf('\r') >= 0 || content.indexOf('\n') >= 0) {
            throw new IllegalArgumentException(fieldName + "不能包含换行符");
        }
    }
}
