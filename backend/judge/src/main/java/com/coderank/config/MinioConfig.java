package com.coderank.config;

import com.coderank.config.properties.MinioProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * MinIO 客户端配置。
 */
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties properties;

    /** 创建 MinIO HTTP 连接池。 */
    @Bean
    public ConnectionPool minioConnectionPool() {
        return new ConnectionPool(
                Math.max(properties.getMaxIdleConnections(), 1),
                properties.getKeepAliveDuration().toMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    /** 创建支持连接复用的 HTTP 客户端。 */
    @Bean
    public OkHttpClient minioHttpClient(ConnectionPool minioConnectionPool) {
        return new OkHttpClient.Builder()
                .connectionPool(minioConnectionPool)
                .connectTimeout(properties.getConnectTimeout())
                .readTimeout(properties.getReadTimeout())
                .writeTimeout(properties.getWriteTimeout())
                .retryOnConnectionFailure(true)
                .build();
    }

    /** 创建 MinIO 客户端。 */
    @Bean
    public MinioClient minioClient(OkHttpClient minioHttpClient) {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .httpClient(minioHttpClient)
                .build();
    }
}
