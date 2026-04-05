package com.coderank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 题目提交表
 * @TableName question_submit
 */
@TableName(value ="question_submit")
@Data
public class QuestionSubmit {
    /**
     * 提交id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 提交用户id
     */
    private Long userId;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 提交代码
     */
    private String code;

    /**
     * 判题状态 0-排队中 1-答案正确 2-编译错误 3-超时 4-超内存 5-答案错误 6-未知错误  
     */
    private Integer status;

    /**
     * 总分数
     */
    private Integer score;

    /**
     * 总耗时 ms
     */
    private Double timeUsed;

    /**
     * 总消耗内存 KB
     */
    private Double memoryUsed;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 提交时间
     */
    private Date submitTime;

}