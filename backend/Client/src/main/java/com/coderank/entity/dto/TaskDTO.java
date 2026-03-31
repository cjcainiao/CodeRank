package com.coderank.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 评测任务请求
 */
@Data
public class TaskDTO {

    /**
     * 代码
     */
    @NotBlank(message = "代码不能为空")
    private String code;

    /**
     * 语言
     */
    @NotBlank(message = "语言不能为空")
    private String language;

    /**
     * 题目id
     */
    @NotNull(message = "题目id不能为空")
    private Long questionId;
}
