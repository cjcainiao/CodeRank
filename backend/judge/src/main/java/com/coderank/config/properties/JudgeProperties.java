package com.coderank.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Judge 连接配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "judge")
public class JudgeProperties {

    /** 评测服务地址。 */
    private String baseUrl = "http://localhost:5050";

    /** 评测服务鉴权令牌。 */
    private String authToken;

    /** HTTP 连接池最大连接数。 */
    private int maxConnections = 16;

    /** 从连接池获取连接的超时时间。 */
    private Duration connectionRequestTimeout = Duration.ofSeconds(1);

    /** 建立 HTTP 连接的超时时间。 */
    private Duration connectTimeout = Duration.ofSeconds(2);

    /** 等待评测结果的超时时间。 */
    private Duration responseTimeout = Duration.ofSeconds(15);

    /** 空闲连接的清理时间。 */
    private Duration idleConnectionTimeout = Duration.ofSeconds(30);

    /** HTTP 连接的最长存活时间。 */
    private Duration connectionTimeToLive = Duration.ofMinutes(5);
}
