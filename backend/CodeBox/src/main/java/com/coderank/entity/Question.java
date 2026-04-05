package com.coderank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

/**
 * 题目表
 *
 * @TableName question
 */
@TableName(value = "question", autoResultMap = true)
@Data
public class Question {
    /**
     * 题目id
     */
    @TableId(type = IdType.AUTO)
    private Long questionId;

    /**
     * 标题
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
     * 题目类型（0 acm赛制, 1 力扣赛制）
     */
    private Integer type;

    /**
     * 提交次数
     */
    private Long submitCount;

    /**
     * 通过次数
     */
    private Long acceptCount;

    /**
     * 题目标签
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /**
     * 代码模板
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<codeTemplate> template;

    /**
     * 样例
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<testCase> testcase;

    /**
     * 难度 1-简单 2-中等 3-困难
     */
    private Integer difficulty;

    /**
     * 创建人
     */
    private String author;

    /**
     * 内存限制(KB),默认64MB
     */
    private Long memory;

    /**
     * 时间限制(ms)
     */
    private Integer timeLimit;

    /**
     * 是否包含测试脚本 0-无、1-有
     */
    private Integer outputScript;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除 0-否 1-是
     */
    private Integer isDelete;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Question other = (Question) that;
        return (this.getQuestionId() == null ? other.getQuestionId() == null : this.getQuestionId().equals(other.getQuestionId()))
                && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
                && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
                && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
                && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
                && (this.getSubmitCount() == null ? other.getSubmitCount() == null : this.getSubmitCount().equals(other.getSubmitCount()))
                && (this.getAcceptCount() == null ? other.getAcceptCount() == null : this.getAcceptCount().equals(other.getAcceptCount()))
                && (this.getTags() == null ? other.getTags() == null : this.getTags().equals(other.getTags()))
                && (this.getTemplate() == null ? other.getTemplate() == null : this.getTemplate().equals(other.getTemplate()))
                && (this.getTestcase() == null ? other.getTestcase() == null : this.getTestcase().equals(other.getTestcase()))
                && (this.getDifficulty() == null ? other.getDifficulty() == null : this.getDifficulty().equals(other.getDifficulty()))
                && (this.getAuthor() == null ? other.getAuthor() == null : this.getAuthor().equals(other.getAuthor()))
                && (this.getMemory() == null ? other.getMemory() == null : this.getMemory().equals(other.getMemory()))
                && (this.getTimeLimit() == null ? other.getTimeLimit() == null : this.getTimeLimit().equals(other.getTimeLimit()))
                && (this.getOutputScript() == null ? other.getOutputScript() == null : this.getOutputScript().equals(other.getOutputScript()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
                && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getQuestionId() == null) ? 0 : getQuestionId().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getSubmitCount() == null) ? 0 : getSubmitCount().hashCode());
        result = prime * result + ((getAcceptCount() == null) ? 0 : getAcceptCount().hashCode());
        result = prime * result + ((getTags() == null) ? 0 : getTags().hashCode());
        result = prime * result + ((getTemplate() == null) ? 0 : getTemplate().hashCode());
        result = prime * result + ((getTestcase() == null) ? 0 : getTestcase().hashCode());
        result = prime * result + ((getDifficulty() == null) ? 0 : getDifficulty().hashCode());
        result = prime * result + ((getAuthor() == null) ? 0 : getAuthor().hashCode());
        result = prime * result + ((getMemory() == null) ? 0 : getMemory().hashCode());
        result = prime * result + ((getTimeLimit() == null) ? 0 : getTimeLimit().hashCode());
        result = prime * result + ((getOutputScript() == null) ? 0 : getOutputScript().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getIsDelete() == null) ? 0 : getIsDelete().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", questionId=").append(questionId);
        sb.append(", title=").append(title);
        sb.append(", content=").append(content);
        sb.append(", description=").append(description);
        sb.append(", type=").append(type);
        sb.append(", submitCount=").append(submitCount);
        sb.append(", acceptCount=").append(acceptCount);
        sb.append(", tags=").append(tags);
        sb.append(", template=").append(template);
        sb.append(", testcase=").append(testcase);
        sb.append(", difficulty=").append(difficulty);
        sb.append(", author=").append(author);
        sb.append(", memory=").append(memory);
        sb.append(", timeLimit=").append(timeLimit);
        sb.append(", outputScript=").append(outputScript);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", isDelete=").append(isDelete);
        sb.append("]");
        return sb.toString();
    }
}