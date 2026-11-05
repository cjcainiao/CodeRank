package com.coderank.entity.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目评测数据表实体。
 */
@Data
@TableName("question_test_data")
public class QuestionTestData {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属题目 */
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
