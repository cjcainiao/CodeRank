package com.coderank.entity.vo.question;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目视图对象。
 */
@Data
public class QuestionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，雪花 ID 转字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
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

    /** 标签主键列表，雪花 ID 转字符串避免前端精度丢失 */
    private List<String> tagIds;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
