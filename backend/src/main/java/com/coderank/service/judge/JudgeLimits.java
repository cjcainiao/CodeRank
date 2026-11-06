package com.coderank.service.judge;

import com.coderank.properties.JudgeProperties;
import lombok.Builder;

/**
 * 运行时资源限额。
 */
@Builder
public record JudgeLimits(

        /** CPU 时间上限，纳秒 */
        long cpuLimit,

        /** 墙钟时间上限，纳秒 */
        long clockLimit,

        /** 内存上限，字节 */
        long memoryLimit,

        /** 进程数上限 */
        long procLimit,

        /** 输出上限，字节 */
        long outputLimit
) {

    // 按题目的时间限制（毫秒）与内存限制（MB）构造限额
    public static JudgeLimits ofQuestion(Integer timeLimitMs, Integer memoryLimitMb, JudgeProperties properties) {
        JudgeProperties.Run run = properties.getRun();
        long timeMs = timeLimitMs == null || timeLimitMs < 1 ? 1000L : timeLimitMs;
        long memoryMb = memoryLimitMb == null || memoryLimitMb < 1 ? 128L : memoryLimitMb;
        long clockFactor = run.getClockFactor() == null || run.getClockFactor() < 1 ? 2L : run.getClockFactor();
        return JudgeLimits.builder()
                .cpuLimit(timeMs * 1_000_000L)
                .clockLimit(timeMs * clockFactor * 1_000_000L)
                .memoryLimit(memoryMb * 1024L * 1024L)
                .procLimit(run.getProcLimit())
                .outputLimit(run.getOutputLimit())
                .build();
    }
}
