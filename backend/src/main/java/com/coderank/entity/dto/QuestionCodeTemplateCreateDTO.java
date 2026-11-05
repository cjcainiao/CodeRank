package com.coderank.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新增题目代码模板请求参数。
 */
@Data
public class QuestionCodeTemplateCreateDTO {

    /** 所属题目 */
    @NotNull(message = "题目 id 不能为空")
    private Long questionId;

    /** 语言：1 Java，2 C++，3 Python，4 Go，5 JavaScript */
    @NotNull(message = "语言不能为空")
    @Min(value = 1, message = "语言取值须为 1 到 5")
    @Max(value = 5, message = "语言取值须为 1 到 5")
    private Integer language;

    /** 初始代码 */
    @NotBlank(message = "初始代码不能为空")
    private String template;
}
