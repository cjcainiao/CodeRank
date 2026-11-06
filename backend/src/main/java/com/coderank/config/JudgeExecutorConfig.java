package com.coderank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 判题线程池配置。
 */
@Configuration
public class JudgeExecutorConfig {

    /** 核心线程数，与评测机最大并发呼应 */
    private static final int CORE_POOL_SIZE = 4;

    /** 最大线程数 */
    private static final int MAX_POOL_SIZE = 8;

    /** 队列容量 */
    private static final int QUEUE_CAPACITY = 200;

    /** 空闲线程存活秒数 */
    private static final int KEEP_ALIVE_SECONDS = 60;

    // 判题任务线程池，队列满后由调用线程执行，避免丢任务
    @Bean("judgeTaskExecutor")
    public ThreadPoolTaskExecutor judgeTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        executor.setThreadNamePrefix("judge-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
