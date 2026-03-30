package com.coderank.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 向响应流中写入消息
 */
public final class HttpResponseUtils {


    /**
     * 设置token
     * @param tokens
     */
    public static void setToken(Map<String, String> tokens) {
        HttpServletResponse response = getResponse();
        tokens.forEach(response::addHeader);
    }

    /**
     * 设置token
     * @param token
     * @param value
     */
    public static void setToken(String token,String value) {
        HttpServletResponse response = getResponse();
        response.addHeader(token,value);
    }


    /**
     * 获取响应流
     * @return
     */
    private static HttpServletResponse getResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = attributes.getResponse();
        return response;
    }
}
