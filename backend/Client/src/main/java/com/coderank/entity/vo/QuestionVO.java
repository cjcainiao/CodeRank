package com.coderank.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.coderank.entity.pojo.codeTemplate;
import com.coderank.entity.pojo.testCase;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 题目相关信息
 */
@Data
public class QuestionVO {
    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 标题
     */
    private String title;

    /**
     * 题目内容
     */
    private String content;

    /**
     * 题目描述
     */
    private String description;

    /**
     * 题目类型（0 acm赛制, 1 力扣赛制）
     */
    private Integer type;

    /**
     * 提交次数
     */
    private Long submitCount;

    /**
     * 通过次数
     */
    private Long acceptCount;

    /**
     * 题目标签
     */
    private List<String> tags;

    /**
     * 代码模板
     */
    private List<codeTemplate> template;

    /**
     * 样例
     */
    private List<testCase> testcase;

    /**
     * 难度 1-简单 2-中等 3-困难
     */
    private Integer difficulty;

    /**
     * 创建人
     */
    private String author;

    /**
     * 内存限制(KB),默认64MB
     */
    private Long memory;

    /**
     * 时间限制(ms)
     */
    private Integer timeLimit;

    /**
     * 是否包含测试脚本 0-无、1-有
     */
    private Integer outputScript;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除 0-否 1-是
     */
    private Integer isDelete;

}