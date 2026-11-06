package com.coderank.entity.vo.judge;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评测机视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeMachineVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，雪花 ID 转字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 机器名称 */
    private String name;

    /** 服务地址 */
    private String endpoint;

    /** 是否已配置认证令牌 */
    private Boolean hasToken;

    /** 状态：0 停用，1 启用 */
    private Integer status;

    /** 调度权重，越大分到的任务越多 */
    private Integer weight;

    /** 最大并发任务数 */
    private Integer maxParallel;

    /** CPU 时间上限，纳秒 */
    private Long cpuLimit;

    /** 墙钟时间上限，纳秒 */
    private Long clockLimit;

    /** 内存上限，字节 */
    private Long memoryLimit;

    /** 进程数上限 */
    private Integer procLimit;

    /** 输出上限，字节 */
    private Long outputLimit;

    /** 最后探测时间 */
    private LocalDateTime lastCheckTime;

    /** 最后探测结果：0 失败，1 成功 */
    private Integer lastCheckOk;

    /** 备注 */
    private String remark;

    /** 指标地址，未开启监控时为空 */
    private String metricsEndpoint;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
