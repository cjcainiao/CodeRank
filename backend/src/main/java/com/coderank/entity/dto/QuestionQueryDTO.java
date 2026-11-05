package com.coderank.entity.dto;

import lombok.Data;

/**
 * 题目分页查询参数。
 */
@Data
public class QuestionQueryDTO {

    /** 页码，从 1 开始 */
    private Long pageNum = 1L;

    /** 每页条数 */
    private Long pageSize = 10L;

    /** 题目标题，模糊匹配 */
    private String title;

    /** 难度：1 简单，2 中等，3 困难 */
    private Integer difficulty;

    /** 判题模式：1 标准输入输出，2 核心代码 */
    private Integer judgeMode;

    /** 状态：0 草稿，1 已发布，2 已下架 */
    private Integer status;

    /** 题目来源，模糊匹配 */
    private String source;

    /** 排序字段：id、createTime、updateTime，为空按 id 排序 */
    private String sortField;

    /** 排序方向：asc 升序，desc 降序，为空按降序排序 */
    private String sortOrder;
}
