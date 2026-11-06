package com.coderank.entity.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 题目代码模板表实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("question_code_template")
public class QuestionCodeTemplate {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属题目 */
    private Long questionId;

    /** 语言：1 Java，2 C++，3 Python，4 Go，5 JavaScript */
    private Integer language;

    /** 初始代码，前端编辑器回显，用户在此编写 */
    private String template;

    /** 桩程序，含 {{code}} 占位符，与用户代码拼接成完整程序 */
    private String stubTemplate;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
