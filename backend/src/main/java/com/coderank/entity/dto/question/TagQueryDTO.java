package com.coderank.entity.dto.question;

import lombok.Data;

/**
 * 标签分页查询参数。
 */
@Data
public class TagQueryDTO {

    /** 页码，从 1 开始 */
    private Long pageNum = 1L;

    /** 每页条数 */
    private Long pageSize = 10L;

    /** 标签名，模糊匹配 */
    private String name;

    /** 排序字段：id、sortOrder、createTime、updateTime，为空按 sortOrder 排序 */
    private String sortField;

    /** 排序方向：asc 升序，desc 降序，为空按升序排序 */
    private String sortOrder;
}
