package com.coderank.entity.vo.judge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 单条 Prometheus 指标。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeMetricVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 指标名称 */
    private String name;

    /** 指标说明，来自 HELP 注释 */
    private String help;

    /** 标签键值对 */
    private Map<String, String> labels;

    /** 指标值，非有限数值时为空 */
    private Double value;
}
