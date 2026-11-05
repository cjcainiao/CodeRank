package com.coderank.entity.dto.question;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新增题目评测数据请求参数。
 */
@Data
public class QuestionTestDataCreateDTO {

    /** 所属题目 */
    @NotNull(message = "题目 id 不能为空")
    private Long questionId;

    /** 测试点编号，不传时按题目已有编号自动递增 */
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
