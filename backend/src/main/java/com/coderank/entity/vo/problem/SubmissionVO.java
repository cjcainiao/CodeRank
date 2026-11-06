package com.coderank.entity.vo.problem;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 提交记录视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，雪花 ID 转字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 所属题目，雪花 ID 转字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long questionId;

    /** 题目标题 */
    private String questionTitle;

    /** 语言标识：java、cpp、python、go、javascript */
    private String language;

    /** 判题结果：0 等待，1 通过，2 答案错误，3 超时，4 内存超限，5 运行错误，6 编译错误，7 系统错误，8 评测中 */
    private Integer status;

    /** 通过测试点数 */
    private Integer passedCount;

    /** 测试点总数 */
    private Integer totalCount;

    /** 最大耗时，毫秒 */
    private Integer timeUsed;

    /** 最大内存，千字节 */
    private Long memoryUsed;

    /** 编译或运行错误信息 */
    private String errorMessage;

    /** 实际使用的评测机 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long judgeMachineId;

    /** 提交代码 */
    private String sourceCode;

    /** 创建时间 */
    private LocalDateTime createTime;
}
