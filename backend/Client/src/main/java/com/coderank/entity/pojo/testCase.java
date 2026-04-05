package com.coderank.entity.pojo;

import lombok.Data;

/**
 * 题目样例
 */
@Data
public class testCase {

    /**
     * 样例id
     */
    private Long id;
    /**
     * 样例输入
     */
    private String case_input;

    /**
     * 样例输出
     */
    private String case_output;

    /**
     * 样例解释
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;
}
