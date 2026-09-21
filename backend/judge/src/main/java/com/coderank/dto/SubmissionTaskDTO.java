package com.coderank.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 评测任务请求。
 */
@Data
public class SubmissionTaskDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 提交任务id。 */
    private Long submissionId;
}
