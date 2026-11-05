package com.coderank.entity.vo.question;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题目评测数据视图对象。
 */
@Data
public class QuestionTestDataVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，雪花 ID 转字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 所属题目，雪花 ID 转字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long questionId;

    /** 测试点编号，从 1 开始，对应 1.in 与 1.out */
    private Integer caseNo;

    /** 输入文件字节数 */
    private Long inputSize;

    /** 输出文件字节数 */
    private Long outputSize;

    /** 是否展示：0 隐藏，1 展示 */
    private Integer visible;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
