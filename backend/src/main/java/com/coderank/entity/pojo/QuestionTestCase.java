package com.coderank.entity.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.coderank.handler.JsonbTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目测试用例表实体。
 */
@Data
@TableName(value = "question_test_case", autoResultMap = true)
public class QuestionTestCase {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属题目 */
    private Long questionId;

    /** 函数入参，核心代码模式用，[[2,7,11,15],9] */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String args;

    /** 期望返回值，核心代码模式用 */
    @TableField(typeHandler = JsonbTypeHandler.class)
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
