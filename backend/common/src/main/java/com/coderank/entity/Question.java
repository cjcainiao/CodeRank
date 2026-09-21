package com.coderank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

/**
 * <p>
 * 题目表
 * </p>
 *
 * @author cainiao
 */
@Data
@Builder
@TableName("question")
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 题目id
     */
    @TableId(value = "question_id", type = IdType.AUTO)
    private Long questionId;

    /**
     * 题目标题
     */
    private String title;

    /**
     * 题目内容
     */
    private String content;

    /**
     * 题目描述
     */
    private String description;

    /**
     * 难度：1简单，2中等，3困难
     */
    private Byte difficulty;

    /**
     * 题目形态：0acm赛制，1力扣赛制
     */
    private Byte type;

    /**
     * 判题模式：0严格比对，1特判，2交互
     */
    private Byte judgeMode;

    /**
     * 题目样例，仅用于展示
     */
    private String sampleCase;

    /**
     * 题目来源
     */
    private String source;

    /**
     * 内存限制(KB)，默认64MB
     */
    private Long memory;

    /**
     * 栈空间限制(KB)，默认8MB
     */
    private Long stackLimit;

    /**
     * 时间限制(ms)
     */
    private Integer timeLimit;

    /**
     * 是否包含测试脚本：0无，1有
     */
    private Byte outputScript;

    /**
     * 输出结果处理脚本内容
     */
    private String outputScriptContent;

    /**
     * 提交次数
     */
    private Long submitCount;

    /**
     * 通过次数
     */
    private Long acceptCount;

    /**
     * 创建人用户id
     */
    private Long authorId;

    /**
     * 题目状态：0下架，1上架
     */
    private Byte status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
