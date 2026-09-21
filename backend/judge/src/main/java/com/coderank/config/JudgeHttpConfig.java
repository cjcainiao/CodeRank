package com.coderank.config;

import com.coderank.config.properties.JudgeProperties;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/**
 * Judge HTTP 客户端配置。
 */
@Configuration
@RequiredArgsConstructor
public class JudgeHttpConfig {

    /** Judge 配置。 */
    private final JudgeProperties properties;

    /** 创建 HTTP 连接池。 */
    @Bean
    public PoolingHttpClientConnectionManager judgeConnectionManager() {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(timeout(properties.getConnectTimeout()))
                .setSocketTimeout(timeout(properties.getResponseTimeout()))
                .setTimeToLive(timeValue(properties.getConnectionTimeToLive()))
                .build();

        int maxConnections = Math.max(properties.getMaxConnections(), 1);
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connectionConfig)
                .setMaxConnTotal(maxConnections)
                .setMaxConnPerRoute(maxConnections)
                .build();
    }

    /** 创建支持连接复用的 HTTP 客户端。 */
    @Bean(destroyMethod = "close")
    public CloseableHttpClient judgeHttpClient(
            PoolingHttpClientConnectionManager judgeConnectionManager) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(timeout(properties.getConnectionRequestTimeout()))
                .setResponseTimeout(timeout(properties.getResponseTimeout()))
                .build();

        return HttpClients.custom()
                .setConnectionManager(judgeConnectionManager)
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                .evictIdleConnections(timeValue(properties.getIdleConnectionTimeout()))
                .disableAutomaticRetries()
                .build();
    }

    /** 创建调用评测服务接口的 RestClient。 */
    @Bean
    public RestClient judgeRestClient(CloseableHttpClient judgeHttpClient) {
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(judgeHttpClient);

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        if (StringUtils.hasText(properties.getAuthToken())) {
            builder.defaultHeader(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + properties.getAuthToken().trim()
            );
        }
        return builder.build();
    }

    /** 将 Duration 转换为请求超时时间。 */
    private Timeout timeout(java.time.Duration duration) {
        return Timeout.ofMilliseconds(duration.toMillis());
    }

    /** 将 Duration 转换为连接存活时间。 */
    private TimeValue timeValue(java.time.Duration duration) {
        return TimeValue.ofMilliseconds(duration.toMillis());
    }
}
