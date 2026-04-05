package com.coderank.entity;

import lombok.Data;

/**
 * 任务实例类
 */
@Data
public class Task {

    /**
     * 任务id
     */
    private Long id;

    /**
     * 代码
     */
    private String code;

    /**
     * 语言
     */
    private String language;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 测试数据
     */
    private String input;
}
