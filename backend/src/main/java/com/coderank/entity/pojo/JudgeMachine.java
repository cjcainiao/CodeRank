package com.coderank.entity.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评测机表实体。
 */
@Data
@TableName("judge_machine")
public class JudgeMachine {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 机器名称 */
    private String name;

    /** 服务地址，如 http://127.0.0.1:5050 */
    private String endpoint;

    /** 认证令牌，未启用认证时为空 */
    private String authToken;

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

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
