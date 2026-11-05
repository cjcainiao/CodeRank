package com.coderank.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * HTTP 客户端配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "http-client")
public class HttpClientProperties {

    /** 评测机专用客户端配置 */
    private Judge judge = new Judge();

    /**
     * 评测机专用客户端配置。
     */
    @Data
    public static class Judge {

        /** 全局最大连接数 */
        private Integer maxTotal = 20;

        /** 单个目标地址最大连接数 */
        private Integer maxPerRoute = 10;

        /** 建立连接超时 */
        private Duration connectTimeout = Duration.ofSeconds(5);

        /** 等待响应超时 */
        private Duration responseTimeout = Duration.ofSeconds(60);

        /** 空闲连接回收时间 */
        private Duration idleEvict = Duration.ofMinutes(5);

        /** 连接保活时间 */
        private Duration keepAlive = Duration.ofMinutes(10);

        /** 空闲超过该时长后使用前校验 */
        private Duration validateAfterInactivity = Duration.ofSeconds(1);
    }
}
