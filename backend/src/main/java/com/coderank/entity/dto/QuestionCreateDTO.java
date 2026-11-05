package com.coderank.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新增题目请求参数。
 */
@Data
public class QuestionCreateDTO {

    /** 题目标题 */
    @NotBlank(message = "题目标题不能为空")
    @Size(max = 200, message = "题目标题长度不能超过 200")
    private String title;

    /** 难度：1 简单，2 中等，3 困难 */
    @Min(value = 1, message = "难度取值须为 1 到 3")
    @Max(value = 3, message = "难度取值须为 1 到 3")
    private Integer difficulty;

    /** 判题模式：1 标准输入输出，2 核心代码 */
    @Min(value = 1, message = "判题模式取值须为 1 或 2")
    @Max(value = 2, message = "判题模式取值须为 1 或 2")
    private Integer judgeMode;

    /** 题目描述，Markdown */
    @NotBlank(message = "题目描述不能为空")
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
    @Min(value = 1, message = "时间限制须大于 0")
    private Integer timeLimit;

    /** 内存限制，MB */
    @Min(value = 1, message = "内存限制须大于 0")
    private Integer memoryLimit;

    /** 题目来源 */
    @Size(max = 100, message = "题目来源长度不能超过 100")
    private String source;

    /** 状态：0 草稿，1 已发布，2 已下架 */
    @Min(value = 0, message = "状态取值须为 0 到 2")
    @Max(value = 2, message = "状态取值须为 0 到 2")
    private Integer status;

    /** 标签主键列表 */
    private List<Long> tagIds;
}
