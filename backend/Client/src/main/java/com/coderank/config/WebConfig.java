package com.coderank.config;

import com.coderank.interceptor.JwtAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {


    private final JwtAuthInterceptor jwtAuthInterceptor;

    /**
     * 注册拦截器
     *
     * @param registry
     */
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/user/flushToken"
                )
                .excludePathPatterns(
                        // Knife4j 核心路径
                        "/doc.html",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        // OpenAPI 文档接口
                        "/v3/api-docs/**",
                        "/v2/api-docs/**", // 兼容 Swagger2
                        // 静态资源和资源列表
                        "/swagger-resources/**",
                        "/webjars/**",
                        // /favicon.ico（浏览器默认请求，避免401）
                        "/favicon.ico"
                );

    }
}
