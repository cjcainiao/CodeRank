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
 * 题目表实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "question", autoResultMap = true)
public class Question {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 题目标题 */
    private String title;

    /** 难度：1 简单，2 中等，3 困难 */
    private Integer difficulty;

    /** 判题模式：1 标准输入输出，2 核心代码 */
    private Integer judgeMode;

    /** 题目描述，Markdown */
    private String description;

    /** 输入格式说明，Markdown，标准输入输出模式用 */
    private String inputFormat;

    /** 输出格式说明，Markdown，标准输入输出模式用 */
    private String outputFormat;

    /** 样例，[{"input":"","output":""}] */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String sampleCases;

    /** 提示，Markdown */
    private String hint;

    /** 时间限制，毫秒 */
    private Integer timeLimit;

    /** 内存限制，MB */
    private Integer memoryLimit;

    /** 题目来源 */
    private String source;

    /** 状态：0 草稿，1 已发布，2 已下架 */
    private Integer status;

    /** 提交次数 */
    private Integer submitCount;

    /** 通过次数 */
    private Integer acceptedCount;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
