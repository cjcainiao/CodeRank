package com.coderank.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.coderank.dto.SubmissionTaskDTO;
import com.coderank.entity.SubmissionTask;
import com.coderank.exception.BusinessException;
import com.coderank.mapper.SubmissionTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.time.Duration;

/**
 * Kafka 评测任务重试配置。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class JudgeKafkaConfig {

    private static final byte TASK_STATUS_COMPLETED = 2;
    private static final byte TASK_STATUS_FAILED = 3;

    private final SubmissionTaskMapper submissionTaskMapper;

    /** 消费失败后进行有限次数重试，耗尽后记录最终失败状态。 */
    @Bean
    public DefaultErrorHandler judgeKafkaErrorHandler(
            @Value("${judge.kafka.max-attempts:3}") int maxAttempts,
            @Value("${judge.kafka.retry-interval:1s}") Duration retryInterval) {
        FixedBackOff backOff = new FixedBackOff(
                Math.max(retryInterval.toMillis(), 0L),
                Math.max(maxAttempts - 1L, 0L)
        );

        return new DefaultErrorHandler(this::recoverTask, backOff);
    }

    /** Kafka 重试耗尽后将任务标记为失败。 */
    private void recoverTask(ConsumerRecord<?, ?> record, Exception exception) {
        Object value = record.value();
        if (!(value instanceof SubmissionTaskDTO message) || message.getSubmissionId() == null) {
            log.error("Kafka 评测任务重试耗尽，消息格式无效", exception);
            return;
        }

        String errorMessage = rootMessage(exception);
        submissionTaskMapper.update(
                null,
                Wrappers.<SubmissionTask>lambdaUpdate()
                        .eq(SubmissionTask::getSubmissionId, message.getSubmissionId())
                        .ne(SubmissionTask::getStatus, TASK_STATUS_COMPLETED)
                        .set(SubmissionTask::getStatus, TASK_STATUS_FAILED)
                        .set(SubmissionTask::getErrorMessage, errorMessage)
        );
        log.error("Kafka 评测任务重试耗尽，submissionId={}", message.getSubmissionId(), exception);
    }

    /** 优先获取业务异常信息，便于写入任务记录。 */
    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        String lastMessage = null;
        while (current != null) {
            if (current.getMessage() != null) {
                lastMessage = current.getMessage();
            }
            if (current instanceof BusinessException && current.getMessage() != null) {
                return current.getMessage();
            }
            current = current.getCause();
        }
        return lastMessage == null ? "评测任务执行失败" : lastMessage;
    }
}
