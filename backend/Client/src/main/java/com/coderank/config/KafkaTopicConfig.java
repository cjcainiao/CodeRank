package com.coderank.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 创建主题
 */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic taskTopic(){
        return new NewTopic("task",1,(short) 1);
    }
}
