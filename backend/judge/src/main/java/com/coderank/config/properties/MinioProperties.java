package com.coderank.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * MinIO 连接配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /** MinIO 服务地址。 */
    private String endpoint = "http://localhost:9000";

    /** MinIO 访问账号。 */
    private String accessKey = "minioadmin";

    /** MinIO 访问密钥。 */
    private String secretKey = "minioadmin";

    /** 默认存储桶名称。 */
    private String bucketName = "coderank";

    /** 连接池最大空闲连接数。 */
    private int maxIdleConnections = 16;

    /** 空闲连接保持时间。 */
    private Duration keepAliveDuration = Duration.ofMinutes(5);

    /** 建立连接超时时间。 */
    private Duration connectTimeout = Duration.ofSeconds(5);

    /** 读取数据超时时间。 */
    private Duration readTimeout = Duration.ofSeconds(30);

    /** 写入数据超时时间。 */
    private Duration writeTimeout = Duration.ofSeconds(30);
}
