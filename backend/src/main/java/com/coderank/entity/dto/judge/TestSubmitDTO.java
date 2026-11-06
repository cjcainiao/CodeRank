package com.coderank.entity.dto.judge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 测试提交请求参数。
 */
@Data
public class TestSubmitDTO {

    /** 目标评测机 */
    @NotNull(message = "评测机不能为空")
    private Long machineId;

    /** 语言标识：java、cpp、python、go、javascript */
    @NotBlank(message = "语言不能为空")
    @Size(max = 20, message = "语言标识长度不能超过 20")
    private String language;

    /** 提交代码 */
    @NotBlank(message = "代码不能为空")
    @Size(max = 65536, message = "代码长度不能超过 65536")
    private String code;

    /** 标准输入，为空时按空输入运行 */
    @Size(max = 65536, message = "标准输入长度不能超过 65536")
    private String input;
}
