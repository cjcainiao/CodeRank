package com.coderank.entity.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.coderank.handler.JsonbTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 代码提交记录表实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "submission", autoResultMap = true)
public class Submission {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属题目 */
    private Long questionId;

    /** 语言标识：java、cpp、python、go、javascript */
    private String language;

    /** 提交代码 */
    private String sourceCode;

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

    /** 各测试点详情，[{"caseNo":1,"passed":true,"time":12}] */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String caseResults;

    /** 实际使用的评测机 */
    private Long judgeMachineId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
