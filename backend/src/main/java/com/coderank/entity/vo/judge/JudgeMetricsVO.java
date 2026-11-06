package com.coderank.entity.vo.judge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 评测机指标快照。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeMetricsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评测机主键 */
    private Long machineId;

    /** 评测机名称 */
    private String machineName;

    /** 服务地址 */
    private String endpoint;

    /** 抓取是否成功 */
    private Boolean available;

    /** 抓取耗时，毫秒 */
    private Long elapsed;

    /** 失败原因，成功时为空 */
    private String message;

    /** 指标列表 */
    private List<JudgeMetricVO> metrics;
}
