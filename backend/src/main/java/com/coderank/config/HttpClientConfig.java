package com.coderank.config;

import com.coderank.properties.HttpClientProperties;
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

/**
 * HTTP 客户端配置。
 */
@Configuration
public class HttpClientConfig {

    // 评测机专用客户端，超时较长且不重试，避免重复执行代码
    @Bean
    public CloseableHttpClient judgeHttpClient(HttpClientProperties properties) {
        HttpClientProperties.Judge config = properties.getJudge();

        // 连接级配置，决定单条连接的超时与存活时间
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(config.getConnectTimeout()))
                .setSocketTimeout(Timeout.of(config.getResponseTimeout()))
                .setValidateAfterInactivity(TimeValue.of(config.getValidateAfterInactivity()))
                .setTimeToLive(TimeValue.of(config.getKeepAlive()))
                .build();

        // 连接池按目标地址分桶，不同评测机各自持有独立连接
        PoolingHttpClientConnectionManager manager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(config.getMaxTotal())
                .setMaxConnPerRoute(config.getMaxPerRoute())
                .setDefaultConnectionConfig(connectionConfig)
                .build();

        // 请求级配置，决定从池中取连接的等待时间与整体响应超时
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(config.getConnectTimeout()))
                .setResponseTimeout(Timeout.of(config.getResponseTimeout()))
                .build();

        return HttpClients.custom()
                .setConnectionManager(manager)
                .setDefaultRequestConfig(requestConfig)
                .evictIdleConnections(TimeValue.of(config.getIdleEvict()))
                .evictExpiredConnections()
                .disableAutomaticRetries()
                .build();
    }
}
