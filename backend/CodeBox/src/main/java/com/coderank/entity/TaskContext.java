package com.coderank.entity;

import lombok.Data;

/**
 * 评测任务上下文
 */
@Data
public class TaskContext {

    /**
     * 任务信息
     */
    private Task task;

    /**
     * 题目相关信息
     */
    private Question question;

    /**
     * 创建任务工作目录
     */
    private String taskPath;

}
