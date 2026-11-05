package com.coderank.entity.vo.question;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题目代码模板视图对象。
 */
@Data
public class QuestionCodeTemplateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，雪花 ID 转字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 所属题目，雪花 ID 转字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long questionId;

    /** 语言：1 Java，2 C++，3 Python，4 Go，5 JavaScript */
    private Integer language;

    /** 初始代码 */
    private String template;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
