package com.coderank.entity.dto.judge;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改评测机请求参数。
 */
@Data
public class JudgeMachineUpdateDTO {

    /** 主键 */
    @NotNull(message = "评测机 id 不能为空")
    private Long id;

    /** 机器名称 */
    @Size(max = 100, message = "机器名称长度不能超过 100")
    private String name;

    /** 服务地址 */
    @Size(max = 255, message = "服务地址长度不能超过 255")
    private String endpoint;

    /** 认证令牌，未启用认证时为空 */
    @Size(max = 255, message = "认证令牌长度不能超过 255")
    private String authToken;

    /** 状态：0 停用，1 启用 */
    @Min(value = 0, message = "状态取值须为 0 或 1")
    @Max(value = 1, message = "状态取值须为 0 或 1")
    private Integer status;

    /** 调度权重，越大分到的任务越多 */
    @Min(value = 1, message = "权重须大于 0")
    private Integer weight;

    /** 最大并发任务数 */
    @Min(value = 1, message = "最大并发数须大于 0")
    private Integer maxParallel;

    /** CPU 时间上限，纳秒 */
    @Min(value = 1, message = "CPU 时间上限须大于 0")
    private Long cpuLimit;

    /** 墙钟时间上限，纳秒 */
    @Min(value = 1, message = "墙钟时间上限须大于 0")
    private Long clockLimit;

    /** 内存上限，字节 */
    @Min(value = 1, message = "内存上限须大于 0")
    private Long memoryLimit;

    /** 进程数上限 */
    @Min(value = 1, message = "进程数上限须大于 0")
    private Integer procLimit;

    /** 输出上限，字节 */
    @Min(value = 1, message = "输出上限须大于 0")
    private Long outputLimit;

    /** 备注 */
    @Size(max = 255, message = "备注长度不能超过 255")
    private String remark;
}
