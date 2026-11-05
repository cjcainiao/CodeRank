package com.coderank.entity.dto.question;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改题目测试用例请求参数。
 */
@Data
public class QuestionTestCaseUpdateDTO {

    /** 主键 */
    @NotNull(message = "用例 id 不能为空")
    private Long id;

    /** 函数入参，JSON 数组，核心代码模式用 */
    private String args;

    /** 期望返回值，JSON 值，核心代码模式用 */
    private String expected;

    /** 标准输入，标准输入输出模式用 */
    private String inputData;

    /** 标准输出，标准输入输出模式用 */
    private String expectedOut;

    /** 是否展示：0 隐藏，1 展示 */
    @Min(value = 0, message = "展示标记取值须为 0 或 1")
    @Max(value = 1, message = "展示标记取值须为 0 或 1")
    private Integer visible;

    /** 排序值，越小越靠前 */
    private Integer sortOrder;
}
