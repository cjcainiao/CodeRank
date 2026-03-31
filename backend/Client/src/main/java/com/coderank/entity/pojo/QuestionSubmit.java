package com.coderank.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 题目提交表
 * @TableName question_submit
 */
@TableName(value ="question_submit")
@Data
public class QuestionSubmit {
    /**
     * 提交id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 提交用户id
     */
    private Long userId;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 提交代码
     */
    private String code;

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

    /**
     * 提交时间
     */
    private Date submitTime;

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
        QuestionSubmit other = (QuestionSubmit) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getQuestionId() == null ? other.getQuestionId() == null : this.getQuestionId().equals(other.getQuestionId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getLanguage() == null ? other.getLanguage() == null : this.getLanguage().equals(other.getLanguage()))
            && (this.getCode() == null ? other.getCode() == null : this.getCode().equals(other.getCode()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getScore() == null ? other.getScore() == null : this.getScore().equals(other.getScore()))
            && (this.getTimeUsed() == null ? other.getTimeUsed() == null : this.getTimeUsed().equals(other.getTimeUsed()))
            && (this.getMemoryUsed() == null ? other.getMemoryUsed() == null : this.getMemoryUsed().equals(other.getMemoryUsed()))
            && (this.getErrorMsg() == null ? other.getErrorMsg() == null : this.getErrorMsg().equals(other.getErrorMsg()))
            && (this.getSubmitTime() == null ? other.getSubmitTime() == null : this.getSubmitTime().equals(other.getSubmitTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getQuestionId() == null) ? 0 : getQuestionId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getLanguage() == null) ? 0 : getLanguage().hashCode());
        result = prime * result + ((getCode() == null) ? 0 : getCode().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getScore() == null) ? 0 : getScore().hashCode());
        result = prime * result + ((getTimeUsed() == null) ? 0 : getTimeUsed().hashCode());
        result = prime * result + ((getMemoryUsed() == null) ? 0 : getMemoryUsed().hashCode());
        result = prime * result + ((getErrorMsg() == null) ? 0 : getErrorMsg().hashCode());
        result = prime * result + ((getSubmitTime() == null) ? 0 : getSubmitTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", questionId=").append(questionId);
        sb.append(", userId=").append(userId);
        sb.append(", language=").append(language);
        sb.append(", code=").append(code);
        sb.append(", status=").append(status);
        sb.append(", score=").append(score);
        sb.append(", timeUsed=").append(timeUsed);
        sb.append(", memoryUsed=").append(memoryUsed);
        sb.append(", errorMsg=").append(errorMsg);
        sb.append(", submitTime=").append(submitTime);
        sb.append("]");
        return sb.toString();
    }
}