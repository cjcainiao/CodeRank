package com.coderank.entity.dto;
;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


/**
 * 题目相关请求
 */
@Data
public class QuestionDTO {

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空",groups = {create.class})
    private String title;

    /**
     * 题目内容
     */
    @NotBlank(message = "题目内容不能为空",groups = {create.class})
    private String content;

    /**
     * 题目描述
     */
    private String description;

    /**
     * 题目类型（0 acm赛制, 1 力扣赛制）
     */
    @NotNull(message = "题目类型不能为空",groups = {create.class})
    private Integer type;


    /**
     * 题目标签
     */
    private String tags;

    /**
     * 难度 1-简单 2-中等 3-困难
     */
    private Integer difficulty;

    /**
     * 内存限制(KB),默认64MB
     */
    private Long memory;

    /**
     * 时间限制(ms)
     */
    private Integer timeLimit;

    /**
     * 是否包含测试脚本 0-无、1-有
     */
    private Integer outputScript;


    /**
     * 创建题目分组
     */
    public interface create{};

}