package com.coderank.service.backend.judge;

import com.coderank.entity.vo.judge.JudgeMetricsVO;

/**
 * 评测机指标监控业务。
 */
public interface JudgeMetricsService {

    // 抓取指定评测机的 Prometheus 指标
    JudgeMetricsVO getMetrics(Long machineId);
}
