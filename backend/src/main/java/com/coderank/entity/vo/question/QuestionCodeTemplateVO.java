package com.coderank.entity.vo.question;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题目代码模板视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    /** 初始代码，前端编辑器回显，用户在此编写 */
    private String template;

    /** 桩程序，含 {{code}} 占位符，与用户代码拼接成完整程序 */
    private String stubTemplate;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
