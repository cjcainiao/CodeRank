package com.coderank.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.okhttp.OkHttpDockerCmdExecFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerConfig {

    @Value("${docker.Host:tcp://localhost:2375}")
    private String Host;

    @Value("${docker.ConnectTimeout:10000}")
    private Integer ConnectTimeout;

    @Value("${docker.ReadTimeout:10000}")
    private Integer ReadTimeout;

    /**
     * 创建docker客户端
     * @return
     */
    @Bean
    public DockerClient createDocker(){
        // 创建Docker配置
        DefaultDockerClientConfig config = new DefaultDockerClientConfig.Builder()
                .withDockerHost(Host)
                .withDockerTlsVerify(false)
                .build();

        // 创建Docker连接池配置

        DockerClient dockerClient = DockerClientBuilder.getInstance(config)
                .withDockerCmdExecFactory(new OkHttpDockerCmdExecFactory()
                        .withConnectTimeout(ConnectTimeout) // 连接超时
                        .withReadTimeout(ReadTimeout) // 读取超时
                )
                .build();

        return dockerClient;
    }
}
