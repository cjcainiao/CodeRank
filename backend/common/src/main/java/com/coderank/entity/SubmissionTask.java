package com.coderank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * <p>
 * 代码提交与评测任务表
 * </p>
 *
 * @author cainiao
 */
@Data
@Builder
@TableName("submission_task")
public class SubmissionTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 提交任务id
     */
    @TableId(value = "submission_id", type = IdType.AUTO)
    private Long submissionId;

    /**
     * 任务类型：0测试运行，1正式评测
     */
    private Byte taskType;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 提交人用户id
     */
    private Long submitterId;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 提交代码
     */
    private String sourceCode;

    /**
     * 代码模板
     */
    private String codeTemplate;

    /**
     * 自定义输入数据，测试运行使用
     */
    private String inputData;

    /**
     * 任务状态：0等待，1运行中，2已完成，3失败
     */
    private Byte status;

    /**
     * 判题结果：1通过，2答案错误，3超时，4内存超限，5运行错误，6编译错误
     */
    private Byte judgeResult;

    /**
     * 程序输出
     */
    private String outputData;

    /**
     * 编译或运行错误信息
     */
    private String errorMessage;

    /**
     * 执行耗时（ms）
     */
    private Integer timeUsed;

    /**
     * 内存使用量（KB）
     */
    private Long memoryUsed;

    /**
     * 判题信息列表
     */
    private String judgeMessageList;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
