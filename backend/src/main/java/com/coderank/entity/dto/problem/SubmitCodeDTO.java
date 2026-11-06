package com.coderank.entity.dto.problem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 做题页提交评测请求参数。
 */
@Data
public class SubmitCodeDTO {

    /** 题目主键 */
    @NotNull(message = "题目不能为空")
    private Long questionId;

    /** 语言标识：java、cpp、python、go、javascript */
    @NotBlank(message = "语言不能为空")
    @Size(max = 20, message = "语言标识长度不能超过 20")
    private String language;

    /** 提交代码 */
    @NotBlank(message = "代码不能为空")
    @Size(max = 65536, message = "代码长度不能超过 65536")
    private String code;
}
