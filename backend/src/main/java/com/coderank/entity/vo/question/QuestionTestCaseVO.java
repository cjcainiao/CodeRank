package com.coderank.entity.vo.question;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题目测试用例视图对象。
 */
@Data
public class QuestionTestCaseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，雪花 ID 转字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 所属题目，雪花 ID 转字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long questionId;

    /** 函数入参，JSON 数组，核心代码模式用 */
    private String args;

    /** 期望返回值，JSON 值，核心代码模式用 */
    private String expected;

    /** 标准输入，标准输入输出模式用 */
    private String inputData;

    /** 标准输出，标准输入输出模式用 */
    private String expectedOut;

    /** 是否展示：0 隐藏，1 展示 */
    private Integer visible;

    /** 排序值，越小越靠前 */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
