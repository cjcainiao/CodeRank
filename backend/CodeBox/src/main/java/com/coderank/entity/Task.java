package com.coderank.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 任务实体
 */
@Data
@AllArgsConstructor
public class Task {
    /**
     * 任务id
     */
    private Long id;

    /**
     * 提交代码
     */
    private String code;

    /**
     * 用户输入数据
     */
    private String input;

    /**
     * 提交语言
     */
    private String language;


    /**
     * 题目ID（自动判题模式使用）
     */
    private Long questionId;
}
