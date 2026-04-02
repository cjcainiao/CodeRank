package com.coderank.entity.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 查询任务状态返回
 */
@Data
public class TaskVO  implements Serializable {

    @JsonIgnore
    private static final long serialVersionUID = 1L;

    /**
     * 判题状态 0-排队中 1-答案正确 2-编译错误 3-超时 4-超内存 5-答案错误 6-未知错误
     */
    private Integer status;

    /**
     * 总得分
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

}
