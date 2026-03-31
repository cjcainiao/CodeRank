package com.coderank.entity.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * 任务传输类
 */
@Data
public class QuestionSubmitBO implements Serializable {

    @JsonIgnore
    private static final long serialVersionUID = 1L;

    /**
     * 任务id
     */
    private Long id;

    /**
     * 代码
     */
    private String code;


    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 语言
     */
    private String language;
}
