package com.coderank.entity.dto.question;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改题目代码模板请求参数。
 */
@Data
public class QuestionCodeTemplateUpdateDTO {

    /** 主键 */
    @NotNull(message = "模板 id 不能为空")
    private Long id;

    /** 语言：1 Java，2 C++，3 Python，4 Go，5 JavaScript */
    @Min(value = 1, message = "语言取值须为 1 到 5")
    @Max(value = 5, message = "语言取值须为 1 到 5")
    private Integer language;

    /** 初始代码，前端编辑器回显，用户在此编写 */
    private String template;

    /** 桩程序，含 {{code}} 占位符，与用户代码拼接成完整程序 */
    private String stubTemplate;
}
