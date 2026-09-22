package com.coderank.client;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.coderank.dto.SubmissionTaskDTO;
import com.coderank.entity.SubmissionTask;
import com.coderank.enums.ErrorCode;
import com.coderank.exception.BusinessException;
import com.coderank.judge.Judge;
import com.coderank.judge.JudgeManager;
import com.coderank.mapper.SubmissionTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka 评测任务消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JudgeTaskConsumer {

    private static final byte TEST_TASK = 0;
    private static final byte JUDGE_TASK = 1;
    private static final byte TASK_STATUS_WAITING = 0;
    private static final byte TASK_STATUS_RUNNING = 1;
    private static final byte TASK_STATUS_COMPLETED = 2;
    private static final byte TASK_STATUS_FAILED = 3;

    private final SubmissionTaskMapper submissionTaskMapper;
    private final JudgeManager judgeManager;

    /** 拉取任务并分发到对应语言沙箱。 */
    @KafkaListener(
            topics = "${judge.kafka.task-topic:judge-task}",
            concurrency = "${judge.kafka.concurrency:4}"
    )
    public void consume(SubmissionTaskDTO message) {
        if (message == null || message.getSubmissionId() == null) {
            log.error("Kafka 评测任务消息为空");
            return;
        }

        Long submissionId = message.getSubmissionId();
        if (!claimTask(submissionId)) {
            SubmissionTask currentTask = submissionTaskMapper.selectById(submissionId);
            if (currentTask == null) {
                log.error("评测任务不存在，submissionId={}", submissionId);
            } else {
                log.info("忽略重复评测消息，submissionId={}，status={}",
                        submissionId, currentTask.getStatus());
            }
            return;
        }

        SubmissionTask task = submissionTaskMapper.selectById(submissionId);
        if (task == null) {
            log.error("评测任务不存在，submissionId={}", submissionId);
            return;
        }

        try {
            Judge judge = judgeManager.getJudge(task.getLanguage());
            if (Byte.valueOf(TEST_TASK).equals(task.getTaskType())) {
                judge.executeTestTask(submissionId);
            } else if (Byte.valueOf(JUDGE_TASK).equals(task.getTaskType())) {
                judge.executeTask(submissionId);
            } else {
                markTaskFailed(submissionId, "不支持的任务类型：" + task.getTaskType());
                return;
            }

            SubmissionTask completedTask = submissionTaskMapper.selectById(submissionId);
            if (completedTask == null
                    || !Byte.valueOf(TASK_STATUS_COMPLETED).equals(completedTask.getStatus())) {
                String errorMessage = completedTask == null
                        ? "评测任务不存在"
                        : completedTask.getErrorMessage();
                throw new BusinessException(
                        ErrorCode.BUSINESS_ERROR,
                        errorMessage == null ? "评测任务未正常完成" : errorMessage
                );
            }
        } catch (RuntimeException exception) {
            String errorMessage = exception.getMessage() == null
                    ? "评测任务分发失败"
                    : exception.getMessage();

            if (!isRetryable(exception)) {
                markTaskFailed(submissionId, errorMessage);
                log.error("评测任务执行失败且不可重试，submissionId={}", submissionId, exception);
                return;
            }

            resetTaskForRetry(submissionId, errorMessage);
            log.error("评测任务分发失败，submissionId={}", submissionId, exception);
            throw exception;
        }
    }

    /** 只有等待中的任务可以进入运行状态，避免重复消息并发执行。 */
    private boolean claimTask(Long submissionId) {
        return submissionTaskMapper.update(
                null,
                Wrappers.<SubmissionTask>lambdaUpdate()
                        .eq(SubmissionTask::getSubmissionId, submissionId)
                        .eq(SubmissionTask::getStatus, TASK_STATUS_WAITING)
                        .set(SubmissionTask::getStatus, TASK_STATUS_RUNNING)
                        .set(SubmissionTask::getErrorMessage, null)
        ) == 1;
    }

    /** 可恢复故障交给 Kafka 重投，重投时允许重新抢占任务。 */
    private void resetTaskForRetry(Long submissionId, String errorMessage) {
        submissionTaskMapper.update(
                null,
                Wrappers.<SubmissionTask>lambdaUpdate()
                        .eq(SubmissionTask::getSubmissionId, submissionId)
                        .ne(SubmissionTask::getStatus, TASK_STATUS_COMPLETED)
                        .set(SubmissionTask::getStatus, TASK_STATUS_WAITING)
                        .set(SubmissionTask::getErrorMessage, errorMessage)
        );
    }

    /** 保存确定性错误，后续相同消息不会重复执行。 */
    private void markTaskFailed(Long submissionId, String errorMessage) {
        submissionTaskMapper.update(
                null,
                Wrappers.<SubmissionTask>lambdaUpdate()
                        .eq(SubmissionTask::getSubmissionId, submissionId)
                        .ne(SubmissionTask::getStatus, TASK_STATUS_COMPLETED)
                        .set(SubmissionTask::getStatus, TASK_STATUS_FAILED)
                        .set(SubmissionTask::getErrorMessage, errorMessage)
        );
    }

    /** 远程服务、文件和系统故障允许重试，参数与代码错误直接结束。 */
    private boolean isRetryable(RuntimeException exception) {
        if (!(exception instanceof BusinessException businessException)) {
            return true;
        }

        int code = businessException.getCode();
        return code == ErrorCode.REMOTE_ERROR.getCode()
                || code == ErrorCode.FILE_DOWNLOAD_FAILED.getCode()
                || code == ErrorCode.FILE_UPLOAD_FAILED.getCode()
                || code == ErrorCode.DB_ERROR.getCode()
                || code == ErrorCode.SYSTEM_ERROR.getCode();
    }
}
