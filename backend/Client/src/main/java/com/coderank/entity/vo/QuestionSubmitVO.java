package com.coderank.entity.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

@Data
public class QuestionSubmitVO implements Serializable {

    @JsonIgnore
    private static final long serialVersionUID = 1L;

    /**
     * 任务id
     */
    private Long id;

    /**
     * 任务状态
     */
    private Integer status = 0;

}
