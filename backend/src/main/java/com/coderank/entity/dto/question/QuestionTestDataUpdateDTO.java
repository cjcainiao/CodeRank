package com.coderank.entity.dto.question;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改题目评测数据请求参数。
 */
@Data
public class QuestionTestDataUpdateDTO {

    /** 主键 */
    @NotNull(message = "评测数据 id 不能为空")
    private Long id;

    /** 测试点编号，从 1 开始，对应 1.in 与 1.out */
    @Min(value = 1, message = "测试点编号须大于 0")
    private Integer caseNo;

    /** 输入文件字节数 */
    @Min(value = 0, message = "输入文件大小不能为负")
    private Long inputSize;

    /** 输出文件字节数 */
    @Min(value = 0, message = "输出文件大小不能为负")
    private Long outputSize;

    /** 是否展示：0 隐藏，1 展示 */
    @Min(value = 0, message = "展示标记取值须为 0 或 1")
    @Max(value = 1, message = "展示标记取值须为 0 或 1")
    private Integer visible;
}
