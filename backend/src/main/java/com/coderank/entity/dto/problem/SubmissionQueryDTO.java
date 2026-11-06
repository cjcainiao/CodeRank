package com.coderank.entity.dto.problem;

import lombok.Data;

/**
 * 提交记录分页查询参数。
 */
@Data
public class SubmissionQueryDTO {

    /** 页码，从 1 开始 */
    private Long pageNum = 1L;

    /** 每页条数 */
    private Long pageSize = 10L;

    /** 题目主键，精确匹配 */
    private Long questionId;

    /** 语言标识，精确匹配 */
    private String language;

    /** 判题结果，精确匹配 */
    private Integer status;

    /** 排序字段：createTime、timeUsed、memoryUsed，为空按创建时间排序 */
    private String sortField;

    /** 排序方向：asc 升序，desc 降序，为空按降序排序 */
    private String sortOrder;
}
