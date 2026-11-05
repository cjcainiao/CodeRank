package com.coderank.entity.dto;

import lombok.Data;

/**
 * 首页题库查询参数。
 */
@Data
public class QuestionBankQueryDTO {

    /** 页码，从 1 开始 */
    private Long pageNum = 1L;

    /** 每页条数 */
    private Long pageSize = 20L;

    /** 题目标题，模糊匹配 */
    private String title;

    /** 难度：1 简单，2 中等，3 困难 */
    private Integer difficulty;

    /** 标签主键 */
    private Long tagId;

    /** 排序字段：id、submitCount、acceptedCount，为空按 id 排序 */
    private String sortField;

    /** 排序方向：asc 升序，desc 降序，为空按降序排序 */
    private String sortOrder;
}
