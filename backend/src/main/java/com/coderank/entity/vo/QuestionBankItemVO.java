package com.coderank.entity.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 首页题库列表项。
 */
@Data
public class QuestionBankItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，雪花 ID 转字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 题目标题 */
    private String title;

    /** 难度：1 简单，2 中等，3 困难 */
    private Integer difficulty;

    /** 判题模式：1 标准输入输出，2 核心代码 */
    private Integer judgeMode;

    /** 提交次数 */
    private Integer submitCount;

    /** 通过次数 */
    private Integer acceptedCount;

    /** 标签名列表 */
    private List<String> tags;
}
